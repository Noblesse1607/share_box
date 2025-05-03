'use client'
 
 import MainLayout from "@/components/mainLayout";
 import EditIcon from "../../../../public/pen-to-square-solid-white.svg";
 import CameraIcon from "../../../../public/camera-solid.svg";
 import ExitICon from "../../../../public/right-from-bracket-solid.svg";
 import UsernameIcon from "../../../../public/address-card-solid.svg";
 import EmailIcon from "../../../../public/envelope-solid.svg";
 import LoadingIcon from "../../../../public/spinner-solid.svg";
 import CloseIcon from "../../../../public/xmark-solid-black.svg";
 import UserIcon from "../../../../public/user-solid-white.svg";
 import ChatIcon from "../../../../public/comment-solid-white.svg";
 import CancleReq from "../../../../public/user-large-slash-solid.svg";
 import axios from "axios";
 import Image from "next/image";
 import React from "react";
 import { useRouter } from "next/navigation";
 import { useEffect, useRef, useState } from "react";
 import PostCard from "@/components/postCard";
 import CommunityExploreCard from "@/components/communityExploreCard";
 import CustomFeedAccCard from "@/components/customFeedAccCard";
 import CommunityCard from "@/components/communityCard";
 import CustomFeedCard from "@/components/customFeedCard";
 import websocketService from "@/websocket/websocket-service";
 import { useFriendReqListContext } from "@/context/FriendReqContext";
import FriendCard from "@/components/friendCard";
import FriendListCard from "@/components/friendListCard";
import ToastMessage from "@/components/toastMessage";
 
 export default function AccountPage({ params }: {params: Promise<{ userId: string }>}) {

     const unwrappedParams = React.use(params);
     const { userId } = unwrappedParams;
     const router = useRouter();
     const userString = sessionStorage.getItem("user");
     const user = userString ? JSON.parse(userString) : {};
     const [avatar, setAvatar] = useState<string>(user.avatar);
     const [username, setUsername] = useState<string>(user.username);
     const postRef = useRef<HTMLDivElement>(null);
     const communityRef = useRef<HTMLDivElement>(null);
     const customFeedRef = useRef<HTMLDivElement>(null);
     const favoriteRef = useRef<HTMLDivElement>(null);
     const friendRef = useRef<HTMLDivElement>(null);
     const editBox = useRef<HTMLDivElement>(null);
     const [data, setData] = useState<any>();
     const [customFeed, setCustomFeed] = useState<any[]>([]);
     const [community, setCommunity] = useState<any[]>([]);
     const [posts, setPosts] = useState<any[]>([]);
     const [favPost, setFavPost] = useState<any[]>([]);
     const [friendList, setFriendList] = useState<any[]>([]);
     const [isLoading, setIsLoading] = useState<boolean>(false);
     const [activePart, setActivePart] = useState<string>("post");
     const [isFriend, setIsFriend] = useState<"ACCEPTED" | "PENDING" | "REJECTED">("REJECTED");
     const [reload, setReload] = useState<number>(0);
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
 
     const handleLogout = async() => {
        await axios.post(
            `http://localhost:8080/sharebox/users/offline/${user.userId}`
        )
         sessionStorage.removeItem("user");
         router.push("/login");
     }
 
     const showEditBox = () => {
         editBox.current?.classList.toggle("hidden");
     }
 
     const handleChange = (e: any) => {
         const newUsername = e.target.value;
         setUsername(newUsername);
     }
 
     const handleUploadImage = async (e: any) => {
         let file = e.target.files[0];
 
         if (!file) {
             console.error("No file selected");
             return;
         }

         setIsLoading(true);
 
         try {
         const formData = new FormData();
         formData.append("avatar", file);
 
        
         const res = await axios.post(
             `http://localhost:8080/sharebox/users/${user.userId}/upload-avatar`,
             formData
         )
         
         if (res.data) {
             const url = res.data.match(/https?:\/\/[^\s]+/);
             const avatarURL = url ? url[0] : null;

             const nonCachedAvatarURL = avatarURL ? 
                `${avatarURL}?t=${new Date().getTime()}` : null;

             const newUser = {
                 ...user,
                 avatar: nonCachedAvatarURL
             }
             sessionStorage.setItem("user", JSON.stringify(newUser));
             setAvatar(nonCachedAvatarURL ?? "");
             setReload(n=>n+1);
             setMessage({
                type: "success",
                message: "Uploading avatar successfully!",
                redirect: false
            });
            setShowMessage(true);
        }
    } catch (error: any) {
        //console.error("Error uploading avatar:", error);
        
        if (error.response) {
            // Server trả về lỗi với status code
            if (error.response.status === 400) {
                setMessage({
                    type: "warning",
                    message: "File không hợp lệ hoặc quá kích thước cho phép",
                    redirect: false
                });
            } else if (error.response.status === 413) {
                setMessage({
                    type: "warning",
                    message: "File quá lớn, vui lòng chọn file nhỏ hơn",
                    redirect: false
                });
            } else {
                setMessage({
                    type: "warning",
                    message: "Có lỗi xảy ra khi tải lên ảnh đại diện",
                    redirect: false
                });
            }
        } else if (error.request) {
            // Không nhận được phản hồi từ server
            setMessage({
                type: "warning",
                message: "Không thể kết nối với máy chủ",
                redirect: false
            });
        } else {
            // Lỗi khi thiết lập request
            setMessage({
                type: "warning",
                message: "Có lỗi xảy ra khi gửi yêu cầu",
                redirect: false
            });
        }
        
        setShowMessage(true);
    } finally {
        setIsLoading(false);
    }
     }
 
     const handleSubmit = async () => {
         if (username != user.username) {
            try {
             const formData = new FormData();
             formData.append("username", username);
             const res = await axios.put(
                 `http://localhost:8080/sharebox/users/update/${user.userId}`,
                 formData
             )
 
             if (res.data.result) {
                 const updatedUser = {
                     ...user,
                     username: username,
                 };
                 sessionStorage.setItem("user", JSON.stringify(updatedUser));
                 editBox.current?.classList.toggle("hidden");
                 setReload(n=>n+1);
                 setMessage({
                    type: "success",
                    message: "Update username successfully!",
                    redirect: false
                });
                setShowMessage(true);
            }
             } catch (error: any) {
                
            if (error.response.status === 400) {
                setMessage({
                    type: "warning",
                    message: "Username existed!",
                    redirect: false
                });
                setShowMessage(true);
            } else {
                setMessage({
                    type: "warning",
                    message: "Error updating account!",
                    redirect: false
                });
                setShowMessage(true);
            } 
           }           
         } else {
             editBox.current?.classList.toggle("hidden");
         }
     }

     const handleAddFriend = async() => {
        setIsFriend("PENDING");
        await axios.post(
            `http://localhost:8080/sharebox/friend/request?requesterId=${user.userId}&receiverId=${userId}`
        )
    }

    const handleCancleREquest = async() => {
        setIsFriend("REJECTED");
        await axios.post(
            `http://localhost:8080/sharebox/friend/cancel-request?requesterId=${user.userId}&receiverId=${userId}`
        )
    }

    const handleUnFriend = async() => {
        try {
            setIsFriend("ACCEPTED");
            await axios.delete(
                `http://localhost:8080/sharebox/friend/${user.userId}?userId=${userId}`
            );
            
            // Reload lại trang sau khi unfriend thành công
            window.location.reload();
            
            // Hoặc nếu bạn sử dụng Next.js router:
            // router.reload();
        } catch (error) {
            console.error("Error unfriending user:", error);
            // Có thể thêm xử lý lỗi ở đây nếu cần
            // Ví dụ: reset lại state nếu có lỗi
            setIsFriend("ACCEPTED"); // Reset lại state nếu API gọi thất bại
        }
    }
 
     useEffect(() => {
         if (user.userId != userId) {
             const getUser = async() => {
                 const res = await axios.get(
                     `http://localhost:8080/sharebox/users/user/${userId}`
                 )
                 if (res.data.result) setData(res.data.result);
             }
             getUser();

             const checkFriend = async() => {
                const res = await axios.get(
                    `http://localhost:8080/sharebox/friend/list?userId=${user.userId}`
                )
                if (res.data.result) {
                    if (res.data.result.some((userFr: any) => userFr.userId.toString() === userId)) {
                        setIsFriend("ACCEPTED");
                    }
                }
            }
            checkFriend();

            const checkPending = async() => {
                const res = await axios.get(
                    `http://localhost:8080/sharebox/friend/pending?receiverId=${userId}`
                )
                if (res.data.result) {
                    if (res.data.result.some((userFr: any) => userFr.userId === user.userId)) {
                        console.log("im in ");
                        
                        setIsFriend("PENDING");
                    } 
                }
            }
            checkPending();
         }
 
         const getCommunityList = async() => {
             const res = await axios.get(
                 `http://localhost:8080/sharebox/community/user/${userId}`
             )
             if (res.data.result) setCommunity(res.data.result);
         }
         getCommunityList();
 
         const getCustomFeedList = async() => {
             const res = await axios.get(
                 `http://localhost:8080/sharebox/custom-feed/user/${userId}`
             )
             if (res.data.result) setCustomFeed(res.data.result);
         }
         getCustomFeedList();
 
         const getPosts = async() => {
             const res = await axios.get(
                 `http://localhost:8080/sharebox/post/get-post/${userId}`
             )
             if (res.data.result) setPosts(res.data.result);
         }
         getPosts();

         const getFavPost = async() => {
            const res = await axios.get(
                `http://localhost:8080/sharebox/favorite/${userId}`
            )
            if (res.data.result) {
                setFavPost(res.data.result);
            }
        }
        getFavPost();

        const getFriendList = async() => {
            const res = await axios.get(
                `http://localhost:8080/sharebox/friend/list?userId=${userId}`
            )
            if (res.data.result) {
                setFriendList(res.data.result);
            }
        }
        getFriendList();

     }, [reload])
 
     useEffect(() => {
         postRef.current?.classList.remove("active-part");
         communityRef.current?.classList.remove("active-part");
         customFeedRef.current?.classList.remove("active-part");
         favoriteRef.current?.classList.remove("active-part");
         friendRef.current?.classList.remove("active-part");
 
         switch (activePart) {
             case "post":
                 postRef.current?.classList.add("active-part");
                 break;
             case "community":
                 communityRef.current?.classList.add("active-part");
                 break;
             case "customFeed":
                 customFeedRef.current?.classList.add("active-part");
                 break;
             case "favorite":
                 favoriteRef.current?.classList.add("active-part");
                 break;
             case "friend":
                 friendRef.current?.classList.add("active-part");    
                 break;
         }
     }, [activePart])

     useEffect(() => {
        websocketService.subscribe(`/topic/friendReq/${user.userId}`, (message) => {
            if (message == "ACCEPTED") {
                setIsFriend("ACCEPTED");
            } else if (message == "REJECTED") {
                setIsFriend("REJECTED");
            }
        })

        return () => {
            websocketService.unsubscribe(`/topic/friendReq/${user.userId}`)
        }
    }, [])
 
     return (
         <MainLayout>
             <main className="w-full flex justify-center select-none p-4">
                 <title>{data ? data?.username : user.username}</title>
                 <div className="relative w-[80%] flex justify-between mt-4">
                     <div className="w-[70%]">
                         <div className="flex gap-4 items-center">
                             <div className="w-[80px] h-[80px] rounded-full overflow-hidden">
                                 <img src={data ? data.avatar : user.avatar} alt="Avatar" className="w-full h-full object-cover"/>
                             </div>
                             <h1 className="text-2xl font-bold">{data ? data.username : user.username}</h1>
                         </div>
                         <div className="mt-6 w-full h-[1px] bg-lineColor"></div>
                         <div className="mt-4 flex gap-4">
                             <div ref={postRef} onClick={()=>setActivePart("post")} className="active-part px-4 h-[50px] flex rounded-full items-center justify-center hover:scale-[1.05] duration-150 cursor-pointer border border-lineColor">
                                 Posts
                             </div>
                             <div ref={communityRef} onClick={()=>setActivePart("community")} className="px-4 h-[50px] flex rounded-full items-center justify-center hover:scale-[1.05] duration-150 cursor-pointer border border-lineColor">
                                 Communities
                             </div>
                             <div ref={customFeedRef} onClick={()=>setActivePart("customFeed")} className="px-4 h-[50px] flex rounded-full items-center justify-center hover:scale-[1.05] duration-150 cursor-pointer border border-lineColor">
                                 CustomFeeds
                             </div>
                             <div ref={favoriteRef} onClick={()=>setActivePart("favorite")} className="px-4 h-[50px] flex rounded-full items-center justify-center hover:scale-[1.05] duration-150 cursor-pointer border border-lineColor">
                                 Favourites
                             </div>
                             <div ref={friendRef} onClick={()=>setActivePart("friend")} className="px-4 h-[50px] flex rounded-full items-center justify-center hover:scale-[1.05] duration-150 cursor-pointer border border-lineColor">
                                 Friends
                             </div>
                         </div>
                         <div className="w-full mt-6">
                             {activePart == "post" ? 
                                 <>
                                     {posts.length == 0 ?
                                         <p className="text-textGrayColor1 font-bold text-center">{data ? "He/She" : "You"} haven't created any posts yet !</p>
                                         :
                                         <div>
                                             {posts.map((post: any, index: number) => {
                                                 return <PostCard key={index} data={post} canNavigate isInCom={false}/>
                                             })}
                                         </div>
                                     }
                                 </>
                                 :
                                 activePart == "community" ?
                                 <>
                                     {community.length == 0 ?
                                         <p className="text-textGrayColor1 font-bold text-center">{data ? "He/She" : "You"} haven't joined any communities yet !</p>
                                         :
                                         <div className="grid grid-cols-2 grid-flow-row gap-4">
                                             {community.map((com: any, index: number) => {
                                                 return <CommunityExploreCard key={index} community={com} isTop/>
                                             })}
                                         </div>
                                     }
                                 </>
                                 :
                                 activePart == "customFeed" ?
                                 <>
                                     {customFeed.length == 0 ?
                                         <p className="text-textGrayColor1 font-bold text-center">{data ? "He/She" : "You"} haven't created any customFeeds yet !</p>
                                         :
                                         <div className="grid grid-cols-2 grid-flow-row gap-4">
                                             {customFeed.map((feed: any, index: number) => {
                                                 return <CustomFeedAccCard key={index} feed={feed} />
                                                })}
                                            </div>
                                        }
                                    </>
                                    :
                                    activePart == "favorite" ?
                                    <>
                                        {favPost.length == 0 ?
                                            <p className="text-textGrayColor1 font-bold text-center">{data ? "He/She" : "You"} haven't saved any posts yet !</p>
                                            :
                                            <div>
                                                {favPost.map((post: any, index: number) => {
                                                    return <PostCard key={post.content} data={post} canNavigate isInCom={false}/>
                                             })}
                                         </div>
                                     }
                                 </>
                                 :
                                 activePart == "friend" ?
                                    <>
                                        {friendList.length == 0 ?
                                            <p className="text-textGrayColor1 font-bold text-center">{data ? "He/She" : "You"} doesn't have any friends yet !</p>
                                            :
                                            <div className="grid grid-cols-2 grid-flow-row gap-4">
                                                {friendList.map((friend: any, index: number) => {
                                                    return <FriendListCard key={index} friend={friend}/>
                                             })}
                                         </div>
                                     }
                                 </>
                                 :
                                 <></>
                             }
                         </div>
                     </div>
                     <div className="sticky top-[100px] h-fit w-[28%] rounded-xl shadow-2xl overflow-hidden">
                         <div className="w-full h-[150px] pink-gradient">
                             
                         </div>
                         <div className="w-full h-[60px] flex gap-4 items-center justify-center border-b border-b-lineColor">
                             {userId == user.userId ? 
                                 <>
                                     <div onClick={showEditBox} className="flex gap-2 w-[80px] h-[30px] items-center justify-center bg-textGrayColor1 rounded-full hover:scale-[1.03] cursor-pointer duration-150">
                                         <Image 
                                             src={EditIcon}
                                             alt="Edit Icon"
                                             className="w-[18px]"
                                         />
                                         <p className="text-sm text-white">Edit</p>
                                     </div>
                                     <div onClick={handleLogout} className="flex gap-2 w-[80px] h-[30px] items-center justify-center bg-warningMessageBackground rounded-full hover:scale-[1.03] cursor-pointer duration-150">
                                         <Image 
                                             src={ExitICon}
                                             alt="Exit Icon"
                                             className="w-[18px]"
                                         />
                                         <p className="text-sm text-white">Exit</p>
                                     </div>
                                 </>
                                 :
                                 isFriend == "REJECTED" ? 
                                 <div onClick={handleAddFriend} className="flex gap-2 w-[130px] h-[30px] items-center justify-center bg-voteDownColor rounded-full hover:scale-[1.03] cursor-pointer duration-150">
                                     <Image
                                         src={UserIcon}
                                         alt="User Icon"
                                         className="w-[12px]"
                                     />
                                     <p className="text-sm text-white">Add Friend</p>
                                 </div>
                                 : isFriend == "PENDING" ? 
                                 <div onClick={handleCancleREquest} className="flex gap-2 w-[150px] h-[30px] items-center justify-center bg-mainColor rounded-full hover:scale-[1.03] cursor-pointer duration-150">
                                     <Image
                                         src={CancleReq}
                                         alt="Cancle Request Icon"
                                         className="w-[12px]"
                                     />
                                     <p className="text-sm text-white">Cancle Request</p>
                                 </div>
                                 :
                                 <div onClick={handleUnFriend} className="flex gap-2 w-[130px] h-[30px] items-center justify-center bg-warningMessageBackground rounded-full hover:scale-[1.03] cursor-pointer duration-150">
                                     <Image
                                         src={ChatIcon}
                                         alt="Chat Icon"
                                         className="w-[12px]"
                                     />
                                     <p className="text-sm text-white">Unfriend</p>
                                 </div>
                             }
                         </div>
                         <div className="p-4">
                             <h3 className="text-textHeadingColor font-bold">INFO</h3>
                             <div className="mt-2 px-2">
                                 <div className="flex gap-2 items-center">
                                     <Image 
                                         src={UsernameIcon}
                                         alt="Username Icon"
                                         className="w-[20px]"
                                     />
                                     <p className="text-sm font-semibold">{data ? data.username : user.username}</p>
                                 </div>
                                 <div className="mt-2 flex gap-2 items-center">
                                     <Image 
                                         src={EmailIcon}
                                         alt="EMail Icon"
                                         className="w-[20px]"
                                     />
                                     <p className="text-sm font-semibold">{data ? data.userEmail : user.userEmail}</p>
                                 </div>
                             </div>
                         </div>
                     </div>
 
                     {/* Edit Box */}
                     <div ref={editBox} className="hidden fixed top-0 left-0 w-full h-[100vh] bg-transparentBlack select-none z-[100] flex items-center justify-center">
                         <div className="relative w-[500px] p-6 rounded-lg bg-white flex flex-col items-center">
                             <Image 
                                 src={CloseIcon}
                                 alt="CLose Icon"
                                 className="absolute top-4 right-4 w-[20px] hover:scale-[1.05] duration-150 cursor-pointer"
                                 onClick={showEditBox}
                             />
                             <section className="w-[100px] h-[100px] rounded-full border border-inputBorderColor relative">
                                 <img
                                     src={avatar}
                                     alt="Avatar"
                                     width={100}
                                     height={100}
                                     className="w-full h-full rounded-full"
                                 />
 
                                 {isLoading ?
                                     <Image
                                         src={LoadingIcon}
                                         alt="Loading Icon"
                                         className="animate-spin absolute w-[25px] bottom-0 right-[10px]"
                                     />
                                     :
                                     <label htmlFor="files">
                                         <Image
                                             src={CameraIcon}
                                             alt="Camera Icon"
                                             className="absolute w-[25px] bottom-0 right-[10px] hover:scale-[1.05] duration-100 cursor-pointer"
                                         />
 
                                         <input id="files" type="file" className="hidden" onChange={(e) => handleUploadImage(e)} accept="image/png, image/jpeg" />
                                     </label>
                                 }
                             </section>
 
                             <input type="text" value={username} onChange={handleChange} className="mt-8 w-[70%] h-[40px] text-sm text-white p-4 rounded-xl bg-buttonColor outline-none placeholder:text-white" placeholder="Username" />
                             
                             <button onClick={handleSubmit} className="mt-6 w-[80px] h-[40px] rounded-md bg-buttonColor text-white hover:scale-[1.03] duration-150">
                                 Save
                             </button>
                         </div>
                     </div>
                 </div>
             </main>
             {showMessage ? <ToastMessage type={message.type} message={message.message} redirect={message.redirect} setShowMessage={setShowMessage} position="top-right"/> : <></>}
         </MainLayout>
     )
 }