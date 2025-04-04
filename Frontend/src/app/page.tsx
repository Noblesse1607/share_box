'use client'

import Link from "next/link";
import PostCard from "@/components/postCard";
import axios from "axios";
import MainLayout from "../components/mainLayout";
import { useEffect, useState } from "react";
import { ChooseTopicDropdown } from "@/components/topicDropdown";
import RecentCard from "@/components/recentCard";
import OnlineCard from "@/components/onlineCard";

export default function Home() {
    const userString = sessionStorage.getItem("user");
    const user = userString ? JSON.parse(userString) : {};
    const recentString = sessionStorage.getItem("recent");
    const recent = recentString ? JSON.parse(recentString) : []; 
    const [topic, setTopic] = useState<string>("0");
    const [posts, setPosts] = useState([]);
 
     const getRecommendPost = async () => {
       const res = await axios.get(`http://localhost:8080/sharebox/post/recommend-posts/${user.userId}`)
       if (res.data.result) {
         console.log(res.data.result);
         setPosts(res.data.result.reverse());
       }
     }
 
     const getPostByTopic = async (topicId: string) => {
       const res = await axios.get(`http://localhost:8080/sharebox/post/${topicId}`)
       if (res.data.result) {
         setPosts(res.data.result.reverse());
       }
     }
 
     useEffect(() => {
       switch (topic) {
         case "1":
           getPostByTopic("1");
           break;
         case "2":
           getPostByTopic("2");
           break;
         case "3":
           getPostByTopic("3");
           break;
         case "4":
           getPostByTopic("4");
           break;
         case "5":
           getPostByTopic("5");
           break;
         case "6":
           getPostByTopic("6");
           break;
         default:
           getRecommendPost();
           break;
       }
     }, [topic]) 
    return (
      <MainLayout>
        <main className="relative w-full p-4">
          <title>Home</title>
          <div className="w-[70%] h-[60px] border-b border-b-lineColor">
             <ChooseTopicDropdown isAll={false} topic={topic} setTopic={setTopic} />
           </div>
           <div className="w-[70%]">
             {posts && posts.map((post: any) => {
               return <PostCard key={post.postId} data={post} canNavigate isInCom={false}/>
             })}
           </div>
           <div className="fixed w-[20%] top-[110px] right-8">
             <div className="w-full border border-lineColor rounded-lg p-5">
               <h1 className="text-xl text-textHeadingColor font-semibold">Recents</h1>
               <div className="w-full max-h-[300px] mt-4 flex flex-col gap-4 overflow-y-scroll com">
                 {recent && recent.length == 0 ? 
                   <p className="text-sm text-center text-textGrayColor1 font-bold">Haven't seen anything !</p>
                   :
                   <>
                     {
                       recent?.reverse().map((recentItem: any, index: number) => {
                         return <RecentCard key={index} data={recentItem}/>
                       })
                     }
                   </>
                 }
               </div>
             </div>
           </div>
           <OnlineCard />
        </main>
      </MainLayout>   
    );
}
