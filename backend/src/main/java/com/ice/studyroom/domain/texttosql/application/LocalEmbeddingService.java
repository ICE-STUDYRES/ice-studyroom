package com.ice.studyroom.domain.texttosql.application;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.ByteBuffer;

@Slf4j
@Service
public class LocalEmbeddingService {

	private ZooModel<String, float[]> model;
	private Predictor<String, float[]> predictor;

	@PostConstruct
	public void initialize() {
		try {
			log.info("로컬 임베딩 모델 로딩 시작...");

			// Sentence Transformers 모델 로드
			Criteria<String, float[]> criteria = Criteria.builder()
				.setTypes(String.class, float[].class)
				.optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
				.optEngine("PyTorch")
				.optProgress(new ProgressBar())
				.build();

			model = criteria.loadModel();
			predictor = model.newPredictor();

			log.info("로컬 임베딩 모델 로딩 완료! (all-MiniLM-L6-v2, 384차원)");

			float[] testEmbedding = predictor.predict("테스트");
			log.info("임베딩 테스트 성공: 차원 = {}", testEmbedding.length);

		} catch (Exception e) {
			log.error("임베딩 모델 로딩 실패", e);
			throw new RuntimeException("임베딩 모델 초기화 실패", e);
		}
	}

	/**
	 * 텍스트를 벡터로 변환
	 */
	public byte[] createEmbedding(String text) {
		try {
			log.debug("임베딩 생성: {}", text);

			float[] embedding = predictor.predict(text);

			return floatArrayToByteArray(embedding);

		} catch (TranslateException e) {
			log.error("임베딩 생성 실패: {}", text, e);
			throw new RuntimeException("임베딩 생성 실패", e);
		}
	}

	/**
	 * float[] → byte[] 변환
	 */
	private byte[] floatArrayToByteArray(float[] floats) {
		ByteBuffer buffer = ByteBuffer.allocate(floats.length * Float.BYTES);
		for (float f : floats) {
			buffer.putFloat(f);
		}
		return buffer.array();
	}

	/**
	 * byte[] → float[] 변환
	 */
	public float[] byteArrayToFloatArray(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		float[] floats = new float[bytes.length / Float.BYTES];
		for (int i = 0; i < floats.length; i++) {
			floats[i] = buffer.getFloat();
		}
		return floats;
	}

	@PreDestroy
	public void cleanup() {
		if (predictor != null) {
			predictor.close();
		}
		if (model != null) {
			model.close();
		}
		log.info("임베딩 모델 리소스 정리 완료");
	}
}
