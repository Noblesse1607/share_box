'use client'
 
 import GoogleIcon from "../../public/earth-asia-solid-gray.svg";
 import OptionIcon from "../../public/bookmark-solid.svg";
 import VoteIcon from "../../public/caret-up-solid.svg";
 import VoteUpIcon from "../../public/caret-up-solid-up.svg";
 import VoteDownIcon from "../../public/caret-up-solid-down.svg";
 import CommentIcon from "../../public/comment-solid-white.svg";
 import ImageIcon from "../../public/image-solid-white.svg";
 import CloseIcon from "../../public/xmark-solid.svg";
 import OptionIConPink from "../../public/bookmark-solid-pink.svg";
 import Arrow from "../../public/angle-up-solid-white.svg";
 import EditPost from "./editCard";
 import ToastMessage from "./toastMessage";
 
 import Image from "next/image";
 import { Music, Game, Anime, Movie, Manga, Sport } from "./topics";
 import { useEffect, useRef, useState } from "react";
 import { useRouter } from "next/navigation";
 import axios from "axios";

 
 export default function PostCard({ data, canNavigate, isInCom }: { data: any, canNavigate: boolean, isInCom: boolean }) {
     const router = useRouter();
     const userString = sessionStorage.getItem("user");
     const user = userString ? JSON.parse(userString) : {};
     const recentString = sessionStorage.getItem("recent");
     const recent = recentString ? JSON.parse(recentString) : [];
     const imageSlide = useRef<HTMLDivElement>(null);
     const [imgIndex, setImgIndex] = useState<number>(0);
     const [voteDown, setVoteDown] = useState<boolean>(false);
     const [voteUp, setVoteUp] = useState<boolean>(false);
     const [score, setScore] = useState<number>(data.voteCount);
     const [cmtCount, setCmtCount] = useState<number>(0);
     const [community, setCommunity] = useState<any>();
     const [isJoin, setIsJoin] = useState<boolean>(false);
     const [isSave, setIsSave] = useState<boolean>(false);
     const [isOwner, setIsOwner] = useState(false);
     const [isEditing, setIsEditing] = useState(false);
     const [postData, setPostData] = useState(data);
     const [showMessage, setShowMessage] = useState<boolean>(false);
     const [message, setMessage] = useState<{
        type: string,
        message: string,
        redirect: boolean
    }>({
        type: "",
        message: "",
        redirect: false
    });
 
     const handleSavePost = async(e: any) => {
         e.stopPropagation();
         //setIsSave(!isSave);
         if (!isSave) {
             await axios.post(
                 `http://localhost:8080/sharebox/favorite/save/${user.userId}?postId=${data.postId}`
             );
             setIsSave(true);
         } else {
             await axios.post(
                 `http://localhost:8080/sharebox/favorite/unsave/${user.userId}?postId=${data.postId}`
             );
             setIsSave(false);
         }
     }

     const handleDeletePost = async (e: any) => {
        e.stopPropagation();
        //console.log("Token:" + user.token);
        //console.log("User object:", user);
        const token = user.token;
        try {
            await axios.delete(`http://localhost:8080/sharebox/post/delete/${data.postId}`
            //, 
            //     {
            //     headers: {
            //         Authorization: `Bearer ${token}`,
            //     },
            // }
        );
            setMessage({
                type: "success",
                message: "Deleted post successfully!",
                redirect: true
            });
            setShowMessage(true);
            setTimeout(() => {
                router.push('/');
            }, 2000);
        } catch (error) {
            console.error("Error delete post:", error);
            setMessage({
                type: "warning",
                message: "Có lỗi xảy ra khi xóa bài post!",
                redirect: false
            });
            setShowMessage(true);
        }
    };
 
     const handleClick = (e: any) => {
         e.stopPropagation();
         imageSlide.current?.classList.toggle("hidden");
         setImgIndex(0);
         imageSlide.current?.focus();
     }
 
     const isVideo = (url: string) => {
         const videoExtensions = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];
         return videoExtensions.some(ext => url.toLowerCase().endsWith(ext));
     };
 
     const formatScore = (score: number) => {
         if (score >= 1000000) {
             return (score / 1000000).toFixed(1) + "m";
         } else if (score >= 1000) {
             return (score / 1000).toFixed(1) + "k";
         }
         return score;
     };
 
     const handlePrevImg = () => {
         setImgIndex(n => (n-1));
     }
 
     const handleNextImg = () => {
         setImgIndex(n => (n+1));
     }
 
     const handleKeyDown = (e: any) => {
         if (e.key == "ArrowLeft" && imgIndex > 0) handlePrevImg();
         if (e.key == "ArrowRight" && imgIndex < (data.media.length - 1)) handleNextImg();
         if (e.key == "Escape") {
             imageSlide.current?.classList.toggle("hidden");
             setImgIndex(0);
         }
     }
 
     const voteApi = async (voteType: string) => {
        await axios.post(
            `http://localhost:8080/sharebox/vote/${user.userId}/${data.postId}?voteType=${voteType}`
        )
    }

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

     const handleNavigate = () => {
        if (canNavigate) {
            const newRecent = recent.filter((obj: any) => JSON.stringify(obj) !== JSON.stringify(data));
            newRecent.push(data);
            sessionStorage.setItem("recent", JSON.stringify(newRecent));
            router.push(`/post/${data.postId}`);   
        }
    }

    const handleGetToCmt = () => {
        const element = document.getElementById("myCmt");
        if (element) {
            element.scrollIntoView({ behavior: "smooth" });
        }
    }

    const handleJoin = async (e: any) => {
        e.stopPropagation();
        setIsJoin(!isJoin);
        if (!isJoin) {
          await axios.post(
            `http://localhost:8080/sharebox/community/add/${user.userId}/${data.communityId}`
          )
        } else {
          await axios.post(
            `http://localhost:8080/sharebox/community/leave/${user.userId}/${data.communityId}`
          )
        }
    }

    const handleEditSuccess = async () => {
        try {
            // Refresh post data after edit
            const res = await axios.get(
                `http://localhost:8080/sharebox/post/${postData.postId}`
            );
            
            if (res.data.result) {
                setPostData(res.data.result);
                
                // Update session storage for recent posts if this post is there
                const recentString = sessionStorage.getItem("recent");
                if (recentString) {
                    const recent = JSON.parse(recentString);
                    const updatedRecent = recent.map((item: any) => 
                        item.postId === postData.postId ? res.data.result : item
                    );
                    sessionStorage.setItem("recent", JSON.stringify(updatedRecent));
                }
            }
        } catch (error) {
            console.error("Error refreshing post data:", error);
        }
    };

    useEffect(() => {
        setIsOwner(user.userId === data.userId);
    }, [user.userId, data.userId]);

    useEffect(() => {
        const checkVote = async () => {
            const res = await axios.get(
                `http://localhost:8080/sharebox/vote/type/${user.userId}/${data.postId}`
            )

            if (res.data.result.voteType) {
                if (res.data.result.voteType == "UPVOTE") setVoteUp(true);
                else if (res.data.result.voteType == "DOWNVOTE") setVoteDown(true);
            }
        }
        checkVote();

        const checkCmt = async () => {
            const res = await axios.get(
                `http://localhost:8080/sharebox/comment/count/${data.postId}`
            )

            if (res.data.result) {
                setCmtCount(res.data.result);
            }
        }
        checkCmt();
    }, []);

    useEffect(() => {
        if (data.communityId != null) {
            const getCom = async() => {
                const res = await axios.get(
                    `http://localhost:8080/sharebox/community/${data.communityId}`
                )
                if (res.data.result) {
                    if (res.data.result.members.some((member: any) => member.userId === user.userId)) {
                        setIsJoin(true);
                    }
                    setCommunity(res.data.result);
                }
                    
            }
            getCom();
        }
    }, [isJoin])

    useEffect(() => {
        const checkSave = async() => {
            const res = await axios.get(
                `http://localhost:8080/sharebox/favorite/${user.userId}`
            )
            if (res.data.result) {
                if (res.data.result.some((favorite: any) => favorite.postId === data.postId)) {
                    setIsSave(true);
                }
            }
        }
        checkSave();
    }, [])
 
     return (
         <>
         {isEditing && (
            <EditPost 
                post={postData} 
                onClose={() => setIsEditing(false)} 
                onSuccess={handleEditSuccess} 
            />
        )}
             <div onClick={handleNavigate} className={`w-full px-4 py-8 border-b border-b-lineColor select-none ${canNavigate && "cursor-pointer hover:bg-postHover"}`}>
                 <div className="flex justify-between">
                 {((data.communityId == null) || (community && isInCom)) &&
                         <div className="flex items-center">
                             <img onClick={(e)=>{
                                     e.stopPropagation();
                                     router.push(`/account/${data.userId}`)}
                                 } src={data.userAvatar} alt="User Avatar" className="w-[50px] h-[50px] shadow-2xl rounded-full"/>
                             <div className="ml-4">
                                 <p className="text-lg font-medium hover:underline">{data.username}</p>
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
                             <div className="ml-6">
                                 {data.postTopics[0].id == 1 && <Music isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 2 && <Game isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 3 && <Anime isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 4 && <Movie isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 5 && <Manga isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 6 && <Sport isHaveBg isSmall canHover={false}/>}
                             </div>
                         </div>


}
                     {community && !isInCom && 
                         <div className="flex items-center">
                             <div onClick={(e)=>{
                                     e.stopPropagation();
                                     router.push(`/community/${community?.communityId}`)}
                                 } className="w-[50px] h-[50px] shadow-2xl rounded-full overflow-hidden">
                                 <img src={community?.avatar} alt="Avatar" className="w-full h-full object-cover"/>
                             </div>
                             <div className="ml-3">
                                 <p onClick={(e)=>{
                                     e.stopPropagation();
                                     router.push(`/community/${community?.communityId}`)}
                                 } className="text-lg font-bold hover:underline">{community?.name}</p>
                                 <div className="flex gap-4">
                                     <p onClick={(e)=>{
                                         e.stopPropagation();
                                         router.push(`/account/${data.userId}`)}
                                     } className="text-sm font-semibold text-textGrayColor1 hover:underline">{data.username}</p>
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
                             <div className="ml-8">
                                 {data.postTopics[0].id == 1 && <Music isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 2 && <Game isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 3 && <Anime isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 4 && <Movie isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 5 && <Manga isHaveBg isSmall canHover={false}/>}
                                 {data.postTopics[0].id == 6 && <Sport isHaveBg isSmall canHover={false}/>}
                             </div>
                            </div>
                          }
                          <div className="flex gap-4 items-center">
                          {isOwner && (
                        <>
                            <div
                onClick={(e) => {
                    e.stopPropagation();
                    setIsEditing(true);
                }}
                className="w-[25px] h-[25px] flex items-center justify-center cursor-pointer hover:scale-[1.05] text-mainColor"
                title="Update"
            >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5">
                    <path d="M21.731 2.269a2.625 2.625 0 00-3.712 0l-1.157 1.157 3.712 3.712 1.157-1.157a2.625 2.625 0 000-3.712zM19.513 8.199l-3.712-3.712-8.4 8.4a5.25 5.25 0 00-1.32 2.214l-.8 2.685a.75.75 0 00.933.933l2.685-.8a5.25 5.25 0 002.214-1.32l8.4-8.4z" />
                </svg>
            </div>
            
            {/* Icon cho Delete */}
            <div
                onClick={handleDeletePost}
                className="w-[25px] h-[25px] flex items-center justify-center cursor-pointer hover:scale-[1.05] text-voteDownColor"
                title="Delete"
            >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5">
                    <path fillRule="evenodd" d="M16.5 4.478v.227a48.816 48.816 0 013.878.512.75.75 0 11-.256 1.478l-.209-.035-1.005 13.07a3 3 0 01-2.991 2.77H8.084a3 3 0 01-2.991-2.77L4.087 6.66l-.209.035a.75.75 0 01-.256-1.478A48.567 48.567 0 017.5 4.705v-.227c0-1.564 1.213-2.9 2.816-2.951a52.662 52.662 0 013.369 0c1.603.051 2.815 1.387 2.815 2.951zm-6.136-1.452a51.196 51.196 0 013.273 0C14.39 3.05 15 3.684 15 4.478v.113a49.488 49.488 0 00-6 0v-.113c0-.794.609-1.428 1.364-1.452z" clipRule="evenodd" />
                </svg>
            </div>
                        </>
                    )} 
                              {(data.communityId != null && !isInCom) ? isJoin ?
                                  <button onClick={handleJoin} className="w-[80px] h-[40px] rounded-full bg-textHeadingColor hover:scale-[1.03] duration-150 text-white">
                                      Joined
                                  </button>
                                  :
                                  <button onClick={handleJoin} className="w-[80px] h-[40px] rounded-full bg-voteDownColor hover:scale-[1.03] duration-150 text-white">
                                      Join
                                  </button>
                                  :
                                  <></>
                              }
                              {isSave ? 
                             <Image 
                                 src={OptionIConPink}
                                 alt="Option Icon Pink"
                                 className="w-[25px] cursor-pointer hover:scale-[1.05]"
                                 onClick={handleSavePost}
                             />
                             :
                             <Image 
                                 src={OptionIcon}
                                 alt="Option Icon"
                                 className="w-[25px] cursor-pointer hover:scale-[1.05]"
                                 onClick={handleSavePost}
                             />
                         }      
                         </div>
                     </div>

                     

                 <div className="mt-6">
                     <h2 className="text-2xl font-semibold">{data.title}</h2>
                     <p className="text-lg mt-2">{data.content}</p>
                     {data.media.length > 0 && 
                         <div onClick={handleClick} className="relative cursor-pointer flex justify-center w-full h-[400px] mt-6 rounded-2xl border overflow-hidden zoom-in">
                             <div className="absolute flex items-center justify-center font-semibold gap-2 bottom-4 right-4 w-[60px] h-[40px] rounded-lg bg-imageBlock z-50">
                                 <Image 
                                     src={ImageIcon}
                                     alt="Image Icon"
                                     className="w-[20px]"
                                 />
                                 <p className="text-white">{data.media.length}</p>
                             </div>
                             {data.media.length > 0 && isVideo(data.media[0]) ? 
                                 <video controls src={data.media && data.media[0]} className="bg-cover max-h-full object-contain"/> :
                                 <img src={data.media && data.media[0]} alt="Image" className="bg-cover max-h-full object-contain" />
                             }
                         </div>
                     }
                 </div>
 
                 <div className="mt-6 flex">
                     <div className={`flex items-center justify-center px-3 gap-4 min-w-[130px] h-[40px] border-[3px] ${voteUp && !voteDown ? "border-mainColor" : voteDown && !voteUp ? "border-voteDownColor" : "border-textGrayColor1"} rounded-full`}>
                         <Image 
                             src={voteUp ? VoteUpIcon : VoteIcon}
                             alt="Vote Up"
                             className="w-[20px] cursor-pointer hover:scale-[1.05]"
                             onClick={handleVoteUp}
                         />
                         <p className={`mb-[4px] font-medium ${voteUp && !voteDown ? "text-mainColor" : voteDown && !voteUp ? "text-voteDownColor" : "text-textGrayColor1"}`}>{formatScore(score)}</p>
                         <Image 
                             src={voteDown ? VoteDownIcon : VoteIcon}
                             alt="Vote Down"
                             className="w-[20px] mb-[5px] rotate-180 cursor-pointer hover:scale-[1.05]"
                             onClick={handleVoteDown}
                         />
                     </div>
                     <div onClick={canNavigate ? handleNavigate : handleGetToCmt} className={`ml-6 w-[100px] h-[40px] gap-2 rounded-full flex items-center justify-center bg-mainColor hover:scale-[1.05] duration-100 cursor-pointer`}>
                         <Image 
                             src={CommentIcon}
                             alt="Comment Icon"
                             className="w-[20px]"
                         />
                         <p className="text-white font-medium mb-[2px]">{cmtCount}</p>
                     </div>
                 </div>
             </div>
             <div  onKeyDown={handleKeyDown} tabIndex={0} ref={imageSlide} className="fixed hidden top-0 left-0 w-full h-[100vh] bg-transparentBlack z-[100] select-none">
                 <Image 
                     src={CloseIcon}                
                     alt="Close Icon"
                     className="absolute top-8 left-8 w-[35px] hover:scale-[1.05] cursor-pointer z-50"
                     onClick={handleClick}
                 />
                 <div className="relative w-full h-[90%] flex items-center justify-center">
                     {imgIndex > 0 && 
                         <Image 
                             src={Arrow}
                             onClick={handlePrevImg}
                             alt="Prev Icon"
                             className="absolute top-[50%] left-4 w-[40px] -rotate-90 hover:scale-[1.05] cursor-pointer"
                         />
                     }
                     {data.media.length > 0 && isVideo(data.media[imgIndex]) ? 
                         <video src={data.media && data.media[imgIndex]} controls className="mt-4 max-w-[80%] max-h-[80%] object-contain" /> :
                         <img src={data.media && data.media[imgIndex]} alt="Image" className="mt-4 max-w-[80%] max-h-[80%] object-contain"/>
                     }
                     {imgIndex < (data.media.length - 1) && 
                         <Image 
                             src={Arrow}
                             onClick={handleNextImg}
                             alt="Next Icon"
                             className="absolute top-[50%] right-4 w-[40px] rotate-90 hover:scale-[1.05] cursor-pointer"
                         />
                     }
                 </div>
                 <div className="w-full h-[10%] py-4 flex gap-6 items-center justify-center">
                     {data.media.map((url: string, index: number) => {
                         if (isVideo(url)) {
                            return <video key={index} src={url} className={`rounded-2xl max-h-[60%] object-contain transition-transform duration-200 ${imgIndex == index ? "border-[3px] border-white" : "opacity-60"} ${data.media.length > 0 && imgIndex == index && "scale-[1.2]"}`}/>
                         } else {
                             return <img key={index} src={url} alt="Image" className={`rounded-2xl max-h-[60%] object-contain transition-transform duration-200 ${imgIndex == index ? "border-[3px] border-white" : "opacity-60"} ${data.media.length > 0 && imgIndex == index && "scale-[1.2]"}`}/>
                         }
                     })}
                 </div>
             </div>
             {showMessage ? <ToastMessage type={message.type} message={message.message} redirect={message.redirect} setShowMessage={setShowMessage} position="top-right"/> : <></>}
         </>
     )
 }