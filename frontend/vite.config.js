import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // 환경 변수 로드
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL, // undefined 방지
          changeOrigin: true,
          secure: false,
        }
      }
    },
    css: {
      postcss: './postcss.config.cjs',
    }
  };
});
