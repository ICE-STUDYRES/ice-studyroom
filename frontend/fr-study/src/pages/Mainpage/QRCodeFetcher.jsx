import { useState, useEffect, useCallback } from "react";
import { useMainpageHandlers } from '../Mainpage/MainpageHandlers';

const useQRCodeFetcher = (resId) => {
  const [qrCode, setQrCode] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { refreshTokens } = useMainpageHandlers();

  const fetchQRCode = useCallback(async (retry = true) => {
    if (!resId || qrCode) return; // ✅ 이미 QR 코드가 있으면 다시 요청하지 않음

    setLoading(true);
    try {
      let accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        setError("로그인이 필요합니다.");
        setLoading(false);
        return;
      }

      const response = await fetch(`/api/reservations/my/${resId}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
      });

      if (response.status === 401 && retry) {
        console.warn("Access token expired. Refreshing tokens...");
        accessToken = await refreshTokens();

        if (accessToken) {
          console.log("New access token after refresh:", accessToken);
          console.log("Retrying fetchQRCode with new access token...");
          return fetchQRCode(false); // 한 번만 재시도
        } else {
          console.error("Token refresh failed. Logging out.");
          setError("세션이 만료되었습니다. 다시 로그인하세요.");
          setLoading(false);
          return;
        }
      }

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const data = await response.text(); // BASE64 인코딩된 QR Code 데이터 받기
      setQrCode(data);  // ✅ QR 코드 데이터 저장
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [resId, qrCode, refreshTokens]); // ✅ `qrCode` 상태도 의존성에 포함하여 중복 호출 방지

  useEffect(() => {
    if (resId && !qrCode) { // ✅ 이미 가져온 데이터가 없을 때만 요청
      fetchQRCode();
    }
  }, [resId, qrCode, fetchQRCode]);

  return { qrCode, error, loading };
};

export default useQRCodeFetcher;
