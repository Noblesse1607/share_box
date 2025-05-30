"use client";

import MainLayout from "@/components/mainLayout";
import PlusIcon from "../../../../public/plus-solid-white.svg";
import EarthIcon from "../../../../public/earth-asia-solid-black.svg";
import MemberIcon from "../../../../public/user-group-solid.svg";
import Image from "next/image";
import { useState, useEffect } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";
import React from "react";
import { ChooseTopicDropdown } from "@/components/topicDropdown";
import PostCard from "@/components/postCard";
import ToastMessage from "@/components/toastMessage";
import TrashIcon from "../../../../public/trash-solid.svg";

type CommunityPageProps = {
  params: Promise<{ communityId: string }>;
};

export default function CommunityPage({ params }: CommunityPageProps) {
  const { communityId } = React.use(params);
  const router = useRouter();

  const userString = sessionStorage.getItem("user");
  const user = userString ? JSON.parse(userString) : {};
  const [community, setCommunity] = useState<any>();
  const [owner, setOwner] = useState<any>();
  const [isJoin, setIsJoin] = useState<boolean>(false);
  const [posts, setPosts] = useState<any[]>([]);
  const [isOwner, setIsOwner] = useState(false);
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
  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false); 
  const [showMembersModal, setShowMembersModal] = useState<boolean>(false);
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null); 

  const handleJoin = async () => {
    //setIsJoin(!isJoin);
    if (!isJoin) {
      await axios.post(
        `http://localhost:8080/sharebox/community/add/${user.userId}/${communityId}`
      );
      setIsJoin(true);
    } else {
      await axios.post(
        `http://localhost:8080/sharebox/community/leave/${user.userId}/${communityId}`
      );
      setIsJoin(false);
    }
  };

  const handleDeleteCommunity = async (e: any) => {
    e.stopPropagation();
    //console.log("Token:" + user.token);
    const token = user.token;
    try {
        await axios.delete(`http://localhost:8080/sharebox/community/delete/${communityId}`
        //   , {
        //     headers: {
        //         Authorization: `Bearer ${token}`,
        //     },
        // }
      );
        setMessage({
            type: "success",
            message: "Deleted community successfully!",
            redirect: true
        });
        setShowMessage(true);
        setTimeout(() => router.push("/"), 2000);
    } catch (error) {
        console.error("Error delete community:", error);
        setMessage({
            type: "warning",
            message: "Error deleting community!",
            redirect: false
        });
        setShowMessage(true);
    }
};

const handleDeletePost = async (postId: string) => {
  try {
    await axios.delete(`http://localhost:8080/sharebox/community/${communityId}/${postId}?userId=${user.userId}`);
    setPosts(posts.filter(post => post.postId !== postId));
    setMessage({
      type: "success",
      message: "Post removed successfully!",
      redirect: false
    });
    setShowMessage(true);
    window.location.reload();
  } catch (error) {
    console.error("Error removing post:", error);
    setMessage({
      type: "warning",
      message: "Error removing post!",
      redirect: false
    });
    setShowMessage(true);
  }
};

const handleRemoveMember = async (memberId: string) => {
  try {
    await axios.post(`http://localhost:8080/sharebox/community/leave/${memberId}/${communityId}`);
    // Update the community object to reflect the change
    setCommunity({
      ...community,
      members: community.members.filter((member: any) => member.userId !== memberId)
    });
    setMessage({
      type: "success",
      message: "Member removed successfully!",
      redirect: true
    });
    setShowMessage(true);
  } catch (error) {
    console.error("Error removing member:", error);
    setMessage({
      type: "warning",
      message: "Error removing member!",
      redirect: false
    });
    setShowMessage(true);
  }
};

  const handleCreatePost = () => {
    sessionStorage.setItem("selectedCommunity", community.communityId);
    router.push('/createpost');
  }

  useEffect(() => {
    const getCommunity = async () => {
      const res = await axios.get(
        `http://localhost:8080/sharebox/community/${communityId}`
      );
      if (res.data.result){
        // console.log("OwnerId la :" + res.data.result.ownerId);
        // console.log("UserId la :" + user.userId); 
        setCommunity(res.data.result);
        setIsOwner(res.data.result.ownerId === user.userId);
      }
    };
    getCommunity();
  }, [isJoin]);

  useEffect(() => {
    if (community && user.userId !== community?.ownerId) {
      const checkUser = async () => {
        const res = await axios.get(
          `http://localhost:8080/sharebox/users/user/${community?.ownerId}`
        );
        if (res.data.result) setOwner(res.data.result);
      };
      checkUser();

      if (community?.members.some((member: any) => member.userId === user.userId)) {
        setIsJoin(true);
      }
    }
  }, [community]);

  //console.log(owner);

  useEffect(() => {
    const getPosts = async () => {
      const res = await axios.get(
        `http://localhost:8080/sharebox/post/community/${communityId}`
      )
      if (res.data.result) setPosts(res.data.result.reverse());
    }
    getPosts();
  }, [])

  return (
    <MainLayout>
       <main className="w-full flex justify-center select-none">
         <title>{community ? community.name : "Share Box"}</title>
         <div className="w-[80%]">
           <div className="w-full h-[300px] bg-textGrayColor1 rounded-xl overflow-hidden">
             {community && <img src={community?.backgroundImg} alt="Background Image" className="w-full h-full object-cover" />}
           </div>
           <div className="relative flex justify-between w-full">
             <div className="absolute -top-10 left-10 w-[120px] h-[120px] bg-textGrayColor1 border-2 border-white rounded-full overflow-hidden">
               {community && <img src={community?.avatar} alt="Avatar" className="w-full h-full object-cover" />}
          </div>
          <h1 className="text-textHeadingColor text-2xl ml-[180px] mt-6 font-bold">{community?.name}</h1>
             <div className="flex mt-6 mr-6 gap-4">
               {(community && (community?.members.some((member: any) => member.userId === user.userId))) &&
                 <div onClick={handleCreatePost} className="w-[150px] h-[40px] rounded-full bg-textHeadingColor text-white flex items-center justify-center gap-2 cursor-pointer hover:scale-[1.05] duration-150">
                   <Image
                     src={PlusIcon}
                     alt="PLus ICon"
                     className="w-[15px]"
                   />
                   <p>Create post</p>
                 </div>
               }
               {(community && community?.ownerId === user.userId) && 
               <>
               <div onClick={() => setShowMembersModal(true)} className="w-[150px] h-[40px] rounded-full bg-blue-500 text-white flex items-center justify-center gap-2 cursor-pointer hover:scale-[1.05] duration-150">
                 <Image
                   src={MemberIcon}
                   alt="Member Icon"
                   className="w-[20px] ml-3"
                 />
                 <p className="ml-3">Manage Members</p>
               </div>
               <div onClick={handleDeleteCommunity} className="w-[150px] h-[40px] rounded-full bg-red-500 text-white flex items-center justify-center gap-2 cursor-pointer hover:scale-[1.05] duration-150">
                   <p>Delete Community</p>
               </div>
               </>
                }
               {(community && community?.ownerId != user.userId) &&
                 <button onClick={handleJoin} className="w-[80px] h-[40px] rounded-full cursor-pointer hover:scale-[1.05] duration-150 bg-textHeadingColor text-white">
                   {isJoin ? "Joined" : "Join"}
                 </button>
               }
          </div>
          </div>
           <div className="w-full mt-16 flex justify-between">
             <div className="w-[70%]">
               {posts.length == 0 ?
                 <p className="text-center font-bold text-textGrayColor1">There aren't any posts yet !</p>
                 :
                 <div className="">
                   {posts.map((post: any) => {
                     const showDeleteButton = isOwner && post.userId !== community.ownerId;
                     return (
                       <div key={post.postId} className="relative">
                         <PostCard data={post} canNavigate isInCom/>
                         {showDeleteButton && (
                           <div 
                             onClick={() => handleDeletePost(post.postId)}
                             className="absolute top-10 right-20 bg-red-500 p-2 rounded-full cursor-pointer hover:bg-red-600 transition"
                           >
                             <Image src={TrashIcon} alt="Delete" width={15} height={15} />
                           </div>
                         )}
                       </div>
                     )
                   })}
                 </div>
               }
             </div>
             <div className="sticky top-[100px] right-0 w-[28%] h-fit bg-boxBackground rounded-md p-4 text-textHeadingColor break-words">
               <h2 className="font-bold text-lg">{community?.name}</h2>
               <p className="text-sm font-semibold">{community?.description}</p>
               <div className="flex mt-4">
                 <div className="flex items-center gap-2">
                   <Image
                     src={EarthIcon}
                     alt="Earth Icon"
                     className="w-[20px]"
                   />
                   <p className="text-sm">
                     {(new Date(community?.createAt).toLocaleString('vi-VN', {
                       hour: '2-digit',
                       minute: '2-digit',
                       day: '2-digit',
                       month: '2-digit',
                       year: 'numeric',
                     }
                     ))}
                   </p>
              </div>
              <div className="ml-4 flex items-center gap-2">
                   <Image
                     src={MemberIcon}
                     alt="Member Icon"
                     className="w-[20px]"
                   />
                   <p className="text-sm">{community?.members && community?.members.length} Members</p>
                 </div>
               </div>
               <div className="mt-4 w-full h-[1px] bg-lineColor"></div>
               <div className="mt-4 w-full">
                 <h2 className="font-bold text-lg">Moderator</h2>
                 <div className="mt-3 flex items-center">
                   <div className="w-[60px] h-[60px] rounded-full bg-textGrayColor1 overflow-hidden">
                     {community &&
                       <img src={owner ? owner.avatar : user.avatar} alt="Avatar" className="w-full h-full object-cover"/>
                     }
                </div>
                <p className="ml-4 font-bold">{community && owner ? owner.username : user.username}</p>
              </div>
            </div>
          </div>
        </div>
       </div>
       {showMembersModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-[500px] max-h-[80vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold">Community Members</h2>
                <button 
                  onClick={() => setShowMembersModal(false)}
                  className="text-gray-500 hover:text-gray-700"
                >
                  ✕
                </button>
              </div>
              <div className="mt-4">
                <h3 className="font-semibold mb-2">Owner</h3>
                <div className="flex items-center p-2 bg-gray-100 rounded-md mb-4">
                  <div className="w-[40px] h-[40px] rounded-full bg-textGrayColor1 overflow-hidden">
                    <img src={owner ? owner?.avatar : user.avatar} alt="Avatar" className="w-full h-full object-cover"/>
                  </div>
                  <p className="ml-3 font-medium">{owner ? owner?.username : user.username}</p>
                </div>
                
                <h3 className="font-semibold mb-2">Members ({community?.members?.length || 0})</h3>
                {community?.members?.filter((member: any) => member.userId !== community.ownerId).map((member: any) => (
                  <div key={member.userId} className="flex items-center justify-between p-2 bg-gray-100 rounded-md mb-2">
                    <div className="flex items-center">
                      <div className="w-[40px] h-[40px] rounded-full bg-textGrayColor1 overflow-hidden">
                        <img src={member.avatar} alt="Avatar" className="w-full h-full object-cover"/>
                      </div>
                      <p className="ml-3 font-medium">{member.username}</p>
                    </div>
                    {isOwner && (
                      <button 
                        onClick={() => handleRemoveMember(member.userId)}
                        className="bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition"
                      >
                        Remove
                      </button>
                    )}
                  </div>
                ))}
                {community?.members?.filter((member: any) => member.userId !== community.ownerId).length === 0 && (
                  <p className="text-gray-500 text-center py-2">No members yet</p>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
      {showMessage ? <ToastMessage type={message.type} message={message.message} redirect={message.redirect} setShowMessage={setShowMessage} position="top-right"/> : <></>}
    </MainLayout>
  );
}
