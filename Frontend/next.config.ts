import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: false,
  /* config options here */
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'eluflzblngwpnjifvwqo.supabase.co',
        port: '', // Nếu không có port đặc biệt, để trống
        pathname: '/storage/v1/object/**', // Cho phép tất cả các tệp con trong thư mục
      },
      {
        protocol: 'https',
        hostname: 'lh3.googleusercontent.com',
        port: '',
        pathname: '/**', // Cho phép tất cả các đường dẫn
      },
    ],
  },
};

export default nextConfig;
