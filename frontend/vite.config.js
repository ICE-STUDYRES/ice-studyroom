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
          target: 'http://localhost:8080', // undefined 방지
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
