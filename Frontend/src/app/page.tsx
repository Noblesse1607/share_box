'use client'

import Link from "next/link";
import Game from "../../public/gamepad-solid.svg";
import MainLayout from "../components/mainLayout";
import Image from "next/image";
import { useState } from "react"; 
import { useRouter } from "next/navigation";

export default function Home() {
    return (
      <MainLayout>
        <main className="w-full">
          <title>Home</title>
            <div>
            </div>
        </main>
      </MainLayout>   
    );
}
