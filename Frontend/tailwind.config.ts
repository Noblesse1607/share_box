import type { Config } from "tailwindcss";

export default {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        blackColor: "var(--black-color)",
        transparentBlack: "var(--transparent-black)",
        lightBlackColor: "var(--light-black-color)",
        textGrayColor1: "var(--text-gray-color-1)",
        textGrayColor2: "var(--text-gray-color-2)",
        textHeadingColor: "var(--text-heading-color)",
        lineColor: "var(--line-color)",
        boxBackground: "var(--box-background)",
        lightWhiteColor: "var(--light-white-color)",
        imageBlock: "var(--image-block)",
        postHover: "var(--post-hover)",

        mainColor: "var(--main-color)",
        buttonColor: "var(--button-color)",
        lightButtonColor: "var(--light-button-color)",
        inputBorderColor: "var(--input-border-color)",
        warningColor: "var(--warning-color)",
        warningMessageBackground: "var(--warning-message-background)",
        imageBackground: "var(--image-backround)",
        onlineColor: "var(--online-color)",
        voteDownColor: "var(--vote-down-color)",
      },
    },
  },
  plugins: [],
} satisfies Config;
