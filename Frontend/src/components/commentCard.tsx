'use client'
 
 import GoogleIcon from "../../public/earth-asia-solid-gray.svg";
 import VoteIcon from "../../public/caret-up-solid.svg";
 import VoteUpIcon from "../../public/caret-up-solid-up.svg";
 import VoteDownIcon from "../../public/caret-up-solid-down.svg";
 import ExtendIcon from "../../public/square-plus-regular.svg";
 import CollapseIcon from "../../public/square-minus-regular.svg";
 import CommentIcon from "../../public/comment-solid.svg";
 import SendIcon from "../../public/paper-plane-solid2.svg";
 import EditIcon from "../../public/pen-to-square-solid.svg";
 import DeleteIcon from "../../public/trash-solid.svg";
 import MoreIcon from "../../public/ellipsis-vertical-solid.svg";
 
 import Image from "next/image";
 import { SetStateAction, useEffect, useState, useRef } from "react";
 import axios from "axios";
 import { useRouter } from "next/navigation";
 
 export default function CommentCard({ data, setReload, isScroll } : { data: any, setReload: (value: SetStateAction<number>) => void, isScroll?: boolean }) {
     const userString = sessionStorage.getItem("user");
     const router = useRouter();
     const user = userString ? JSON.parse(userString) : {};
     const [isExtend, setIsExtend] = useState<boolean>(false);
     const [isReply, setIsReply] = useState<boolean>(false);
     const [replyContent, setReplyContent] = useState<string>("");
     const [voteDown, setVoteDown] = useState<boolean>(false);
     const [voteUp, setVoteUp] = useState<boolean>(false);
     const [score, setScore] = useState<number>(data.voteCommentCount);
     const id = sessionStorage.getItem("commentId");
     const isChosen = sessionStorage.getItem("commentId") && id?.includes(data.commentId.toString());
     const [showOptions, setShowOptions] = useState<boolean>(false);
     const [isEditing, setIsEditing] = useState<boolean>(false);
     const [editContent, setEditContent] = useState<string>(data.content);
     const optionsRef = useRef<HTMLDivElement>(null);

     const isCommentOwner = user.userId === data.userId;

     useEffect(() => {
        // Click outside handler for options menu
        function handleClickOutside(event: MouseEvent) {
            if (optionsRef.current && !optionsRef.current.contains(event.target as Node)) {
                setShowOptions(false);
            }
        }
        
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);
 
     const handleSendReply = async() => {
         if (replyContent != "") {
             const res = await axios.post(
                 `http://localhost:8080/sharebox/comment/create/${user.userId}/${data.postId}`,
                 {
                     "content": replyContent,
                     "parentCommentId": data.commentId
                 }
             )
 
             if (res.data.code == 1000) {
                 setIsReply(!isReply);
                 setReplyContent("");
                 setIsExtend(true);
                 setReload(n => n+1);
             }
         }
     }
 
     const voteApi = async (voteType: string) => {
         await axios.post(
             `http://localhost:8080/sharebox/vote-comment/${user.userId}/${data.postId}/${data.commentId}?voteType=${voteType}`
         )
     }
 
     const formatScore = (score: number) => {
         if (score >= 1000000) {
             return (score / 1000000).toFixed(1) + "m";
         } else if (score >= 1000) {
             return (score / 1000).toFixed(1) + "k";
         }
         return score;
     };
 
     const handleVoteUp = async (e: any) => {
         e.stopPropagation();
         voteApi("UPVOTE");
         if (voteUp) {
             setScore(n => n-1);
             setVoteUp(false);
         } else if (voteDown) {
             setScore(n => n+2);
             setVoteDown(false);
             setVoteUp(true);
         } else {
             setScore(n => n+1);
             setVoteUp(true);
         }
     }
 
     const handleVoteDown = (e: any) => {
         e.stopPropagation();
         voteApi("DOWNVOTE");
         if (voteDown) {
             setScore(n => n+1);
             setVoteDown(false);
         } else if (voteUp) {
             setScore(n => n-2);
             setVoteUp(false);
             setVoteDown(true);
         } else {
             setScore(n => n-1);
             setVoteDown(true);
         }
     }

     const handleEditComment = async () => {
        try {
            if (editContent.trim() === "") return;
            
            const res = await axios.patch(
                `http://localhost:8080/sharebox/comment/${data.commentId}`,
                {
                    "content": editContent
                }
            );

            if (res.data.code === 1000 || res.status === 200) {
                data.content = editContent;
                setIsEditing(false);
                setShowOptions(false);
                setReload(n => n+1);
            }
        } catch (error) {
            console.error("Failed to edit comment:", error);
        }
    }

    const handleDeleteComment = async () => {
        try {
            const res = await axios.delete(
                `http://localhost:8080/sharebox/comment/${data.commentId}`
            );

            if (res.data.code === 1000 || res.status === 200) {
                setReload(n => n+1);
            }
        } catch (error) {
            console.error("Failed to delete comment:", error);
        }
    }
 
     useEffect(() => {
         const checkVote = async () => {
             const res = await axios.get(
                 `http://localhost:8080/sharebox/vote-comment/type/${user.userId}/${data.postId}/${data.commentId}`
             )
 
             if (res.data.result.voteType) {
                 if (res.data.result.voteType == "UPVOTE") setVoteUp(true);
                 else if (res.data.result.voteType == "DOWNVOTE") setVoteDown(true);
             }
         }
         checkVote();
     }, [])
 
     return (
        <div id={`id${data.commentId}`} className={`w-full p-4 select-none ${(isScroll && isChosen) && 'flash-bg'}`}>
            <div className="flex gap-4 justify-between">
                <div className="flex gap-4">
                    <div className="w-[40px] h-[40px] rounded-full overflow-hidden flex items-center justify-center">
                        <img src={data.avatar} alt="userAvatar" className="w-full h-full object-cover"/>
                    </div>
                    <div className="flex items-center gap-4">
                        <h1 onClick={(e)=>{
                                        e.stopPropagation();
                                        router.push(`/account/${data.userId}`)}
                                    }>{data.username}</h1>
                        <div className="flex gap-1">
                            <Image
                                src={GoogleIcon}
                                alt="Google Icon"
                                className="w-[12px]"
                            />
                            <p className="text-sm text-textGrayColor1">
                                {(new Date(data.createAt).toLocaleString('vi-VN', {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                    day: '2-digit',
                                    month: '2-digit',
                                    year: 'numeric',
                                }
                                ))}
                            </p>
                        </div>
                    </div>
                </div>
                {isCommentOwner && (
                    <div className="relative" ref={optionsRef}>
                        <div 
                            className="w-[28px] h-[28px] rounded-full flex items-center justify-center cursor-pointer"
                            onClick={() => setShowOptions(!showOptions)}
                        >
                            <Image
                                src={MoreIcon}
                                alt="More Options"
                                className="w-[8px] translate-y-[10px]"
                            />
                        </div>
                        {showOptions && (
                            <div className="absolute right-0 mt-1 bg-white rounded-md shadow-lg py-2 z-10 w-32">
                                <div 
                                    className="px-4 py-2 hover:bg-slate-100 cursor-pointer flex items-center gap-2"
                                    onClick={() => {
                                        setIsEditing(true);
                                        setShowOptions(false);
                                    }}
                                >
                                    <Image
                                        src={EditIcon}
                                        alt="Edit"
                                        className="w-[14px]"
                                    />
                                    <span>Edit</span>
                                </div>
                                <div 
                                    className="px-4 py-2 hover:bg-slate-100 cursor-pointer text-red-500 flex items-center gap-2"
                                    onClick={handleDeleteComment}
                                >
                                    <Image
                                        src={DeleteIcon}
                                        alt="Delete"
                                        className="w-[14px]"
                                    />
                                    <span>Delete</span>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
            <div className={`mt-2 w-full ml-[19px] pl-[36px] ${data.childComments.length != 0 && "border-l border-l-lineColor"}`}>
                {isEditing ? (
                    <div className="w-full mb-3">
                        <textarea 
                            value={editContent} 
                            onChange={(e) => setEditContent(e.target.value)}
                            className="w-full p-3 border border-lineColor outline-none rounded-md text-sm"
                            rows={3}
                        />
                        <div className="flex justify-end gap-2 mt-2">
                            <button 
                                onClick={() => {
                                    setIsEditing(false);
                                    setEditContent(data.content);
                                }}
                                className="px-4 py-1 text-sm rounded-full hover:bg-slate-200"
                            >
                                Cancel
                            </button>
                            <button 
                                onClick={handleEditComment}
                                className="px-4 py-1 text-sm bg-mainColor text-white rounded-full hover:opacity-90"
                            >
                                Save
                            </button>
                        </div>
                    </div>
                ) : (
                    <p>{data.content}</p>
                )}
            </div>
            <div className="mt-2 relative pl-[56px] flex">
                {data.childComments.length != 0 && 
                    <Image 
                        src={isExtend ? CollapseIcon : ExtendIcon}
                        alt="icon"
                        className="absolute left-[9px] top-[4px] w-[20px] cursor-pointer"
                        onClick={() => setIsExtend(!isExtend)}
                    />
                }
                <div className="flex gap-4 items-center">
                    <Image 
                        src={voteUp ? VoteUpIcon : VoteIcon}
                        alt="Voteup Icon"
                        className="w-[20px] cursor-pointer hover:scale-[1.05] duration-150"
                        onClick={handleVoteUp}
                    />
                    <div className="w-[20px]">
                        <p className={`text-center mb-[5px] ${voteUp && !voteDown ? "text-mainColor" : voteDown && !voteUp ? "text-voteDownColor" : "text-textGrayColor1"}`}>{formatScore(score)}</p>
                    </div>
                    <Image 
                        src={voteDown ? VoteDownIcon : VoteIcon}
                        alt="Voteup Icon"
                        className="w-[20px] rotate-180 mb-[5px] cursor-pointer hover:scale-[1.05] duration-150"
                        onClick={handleVoteDown}
                    />
                </div>
                <div onClick={() => setIsReply(!isReply)} className="px-4 cursor-pointer rounded-full hover:bg-slate-200 flex ml-6 gap-2 items-center">
                    <Image 
                        src={CommentIcon}
                        alt="Comment Icon"
                        className="w-[18px]"
                    />
                    <p className="mb-[5px]">Reply</p>
                </div>
            </div>
            {isReply &&
                <div className="pl-[56px] w-full h-[80px] p-4 flex items-center gap-4">
                    <input value={replyContent} onChange={(e) => setReplyContent(e.target.value)} type="text" className=" text-sm w-[90%] h-[40px] p-4 border border-lineColor outline-none rounded-full" placeholder="Add a reply" />
                    <div onClick={handleSendReply} className="w-[40px] h-[40px] rounded-full hover:bg-slate-200 flex items-center justify-center cursor-pointer">
                        <Image
                            src={SendIcon}
                            alt="Send Icon"
                            className="w-[20px]"
                        />
                    </div>
                </div>
            }
            {isExtend &&
                <div className="ml-[19px] pl-[9px] relative border-l border-l-lineColor">
                    {data.childComments.length != 0 && 
                        data.childComments.reverse().map((cmt: any, index: number) => {
                            return <CommentCard key={index} data={cmt} setReload={setReload} isScroll={isScroll}/>
                        })
                    }
                </div>
            }
        </div>
    )
 }