"use client"; 

import NavigateIcon from "../../../../public/arrow-right-solid.svg";
import SendIcon from "../../../../public/paper-plane-solid-pink.svg";

import MainLayout from "@/components/mainLayout";
import PostCard from "@/components/postCard";
import axios from "axios";
import { useRouter, useParams } from "next/navigation"; 
import { useEffect, useState } from "react";
import Image from "next/image";
import CommentCard from "@/components/commentCard";

export default function PostPage() {
    const router = useRouter();
    const params = useParams();
    const postId = params?.postId as string; // Lấy postId từ useParams()
    
    const [user, setUser] = useState<any>(null);
    const [post, setPost] = useState<any>(null);
    const [comment, setComment] = useState<any>(null);
    const [cmtContent, setCmtContent] = useState<string>("");
    const [reload, setReload] = useState<number>(0);
    
    // Chỉ lấy thông tin user khi chạy trên client
    useEffect(() => {
        if (typeof window !== "undefined") {
            const userString = sessionStorage.getItem("user");
            setUser(userString ? JSON.parse(userString) : {});
        }
    }, []);

    const handleNavigate = () => {
        router.back();
    };

    const handleSendComment = async () => {
        if (cmtContent !== "" && user) {
            const res = await axios.post(
                `http://localhost:8080/sharebox/comment/create/${user.userId}/${postId}`,
                { content: cmtContent }
            );

            if (res.data.code === 1000) {
                setReload((n) => n + 1);
            }
        }
    };

    useEffect(() => {
        setCmtContent("");
        const fetchData = async () => {
            const res = await axios.get(`http://localhost:8080/sharebox/post/get/${postId}`);
            if (res.data.result) {
                setPost(res.data.result);
            }
        };
        fetchData();

        const getComment = async () => {
            const res = await axios.get(`http://localhost:8080/sharebox/comment/parent/${postId}`);
            if (res.data.result) {
                setComment(res.data.result.reverse());
            }
        };
        getComment();
    }, [reload, postId]);

    return (
        <MainLayout>
            {post && (
                <main className="relative w-full min-h-[130vh] flex justify-center">
                    <title>{post?.title}</title>
                    <div
                        onClick={handleNavigate}
                        className="absolute -rotate-90 left-[30px] top-[36px] cursor-pointer w-[50px] h-[50px] flex items-center justify-center rounded-full hover:bg-slate-200"
                    >
                        <Image src={NavigateIcon} alt="Navigate Icon" className="w-[20px]" />
                    </div>
                    <div className="w-[70%]">
                        <PostCard data={post} canNavigate={false} />
                        <div id="myCmt" className="w-full h-[80px] p-4 flex items-center justify-between">
                            <div className="w-[40px] h-[40px] rounded-full overflow-hidden">
                                <img src={post?.userAvatar} alt="userAvatar" />
                            </div>
                            <input
                                value={cmtContent}
                                onChange={(e) => setCmtContent(e.target.value)}
                                type="text"
                                className="w-[85%] h-[60px] p-4 border border-lineColor outline-none rounded-full"
                                placeholder={`Add a comment with ${post?.username}`}
                            />
                            <div
                                onClick={handleSendComment}
                                className="w-[60px] h-[60px] rounded-full hover:bg-slate-200 flex items-center justify-center cursor-pointer"
                            >
                                <Image src={SendIcon} alt="Send Icon" className="w-[30px]" />
                            </div>
                        </div>
                        <div className="mt-4">
                            {comment && comment.length === 0 ? (
                                <p className="text-lg text-center text-textGrayColor1 font-bold">
                                    There aren't any comments yet!
                                </p>
                            ) : (
                                <>
                                    {comment?.map((cmt: any, index: number) => (
                                        <CommentCard key={index} data={cmt} setReload={setReload} />
                                    ))}
                                </>
                            )}
                        </div>
                    </div>
                </main>
            )}
        </MainLayout>
    );
}
