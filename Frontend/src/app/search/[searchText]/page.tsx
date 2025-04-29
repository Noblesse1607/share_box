'use client'
 
 import CommunityCard from "@/components/communityCard";
 import MainLayout from "@/components/mainLayout";
 import PostCard from "@/components/postCard";
 import axios from "axios";
 import React from "react";
 import { useEffect, useState } from "react";
 import UserCard from "@/components/usersCard";
 
 
 type SearchPageProps = {
     params: Promise<{
       searchText: string;
     }>;
   };
 
 export default function SearchPage({ params }: SearchPageProps) {
     const { searchText } = React.use(params);
     const [searchRes, setSearchRes] = useState<any[]>([]);
     const [coms, setComs] = useState<any[]>([]);
     const [users, setUsers] = useState<any[]>([]);
     const [activeTab, setActiveTab] = useState<'posts' | 'users'>('posts');
 
     useEffect(() => {
         const getPosts = async() => {
             const res = await axios.post(
                 "http://localhost:8080/sharebox/post/search",
                 {
                     keyword: searchText
                 }
             )
             console.log("Post response:", res.data);
             if (res.data.result) setSearchRes(res.data.result);
         } 
         getPosts();
 
         const getComs = async() => {
             const res = await axios.post(
                 "http://localhost:8080/sharebox/community/search",
                 {
                     keyword: searchText
                 }
             )
             //console.log("Community response:", res.data);
             if (res.data.result) setComs(res.data.result);
         }
         getComs();

         const getUsers = async() => {
            const res = await axios.post(
                "http://localhost:8080/sharebox/users/search",
                {
                    keyword: searchText
                }
            )
            console.log("User response:", res.data);
            if (res.data.result) setUsers(res.data.result);
        }
        getUsers();

     }, [searchText])
 
     return (
         <MainLayout>
             <main className="w-full select-none px-4">
                 <title>{searchText}</title>
                 <div className="flex justify-between w-full mt-4">
                     <div className="w-[70%]">
                         <h1 className="text-2xl font-bold text-textHeadingColor">Results for: "{searchText}"</h1>
                         <div className="mt-4 flex space-x-4 border-b border-lineColor">
                            <button 
                                onClick={() => setActiveTab('posts')}
                                className={`pb-2 px-4 font-semibold ${activeTab === 'posts' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-textGrayColor1'}`}
                            >
                                Posts {searchRes.length > 0 && `(${searchRes.length})`}
                            </button>
                            <button 
                                onClick={() => setActiveTab('users')}
                                className={`pb-2 px-4 font-semibold ${activeTab === 'users' ? 'border-b-2 border-blue-500 text-blue-500' : 'text-textGrayColor1'}`}
                            >
                                Users {users.length > 0 && `(${users.length})`}
                            </button>
                        </div>
                        
                        <div className="mt-6">
                            {activeTab === 'posts' ? (
                                <>
                                    {searchRes.length === 0 ? (
                                        <p className="text-lg font-semibold text-textGrayColor1">No posts found!</p>
                                    ) : (
                                        <>  
                                            {searchRes.map((post: any) => (
                                                <PostCard key={post.postId} data={post} canNavigate isInCom={false}/>
                                            ))}
                                        </>
                                    )}
                                </>
                            ) : (
                                <>
                                    {users.length === 0 ? (
                                        <p className="text-lg font-semibold text-textGrayColor1">No users found!</p>
                                    ) : (
                                        <div className="flex flex-wrap gap-4">
                                            {users.map((user: any) => (
                                                <UserCard key={user.userId} user={user} />
                                            ))}
                                        </div>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                     <div className="sticky top-[100px] right-0 w-[28%] h-fit rounded-lg p-4 border border-lineColor bg-white">
                         <h1 className="font-bold text-textHeadingColor">COMMUNITIES</h1>
                         <div className="mt-2">
                             {coms.length == 0 ?
                                 <p className="text-sm text-textGrayColor1 font-semibold text-center">No communities found !</p>
                                 :
                                 <>
                                     {coms.map((com: any, index: number) => {
                                         return <CommunityCard key={index} community={com}/>
                                     })}
                                 </>
                             }
                         </div>
                     </div>
                 </div>
             </main>
         </MainLayout>
     )
 }