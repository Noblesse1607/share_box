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
    ],
  },
};

export default nextConfig;
