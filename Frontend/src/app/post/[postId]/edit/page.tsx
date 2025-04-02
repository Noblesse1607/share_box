"use client";

import { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import axios from "axios";
import Image from "next/image";
import CloseIcon from "../../../../../public/xmark-solid.svg";
import MainLayout from "@/components/mainLayout";

export default function EditPostPage() {
    const router = useRouter();
    const params = useParams();
    const postId = params?.postId as string;
    
    const [user, setUser] = useState<any>(null);
    const [post, setPost] = useState<any>(null);
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [media, setMedia] = useState<string[]>([]);
    const [filesToUpload, setFilesToUpload] = useState<File[]>([]);
    const [mediaToRemove, setMediaToRemove] = useState<string[]>([]);

    useEffect(() => {
        if (typeof window !== "undefined") {
            const userString = sessionStorage.getItem("user");
            setUser(userString ? JSON.parse(userString) : {});
        }
    }, []);

    useEffect(() => {
        const fetchPost = async () => {
            try {
                const res = await axios.get(`http://localhost:8080/sharebox/post/get/${postId}`);
                if (res.data.result) {
                    setPost(res.data.result);
                    setTitle(res.data.result.title);
                    setContent(res.data.result.content);
                    setMedia(res.data.result.media || []);
                }
            } catch (error) {
                console.error("Error fetching post:", error);
            }
        };
        fetchPost();
    }, [postId]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            setFilesToUpload([...filesToUpload, ...Array.from(e.target.files)]);
        }
    };

    const handleRemoveMedia = (url: string) => {
        setMedia(media.filter(m => m !== url));
        setMediaToRemove([...mediaToRemove, url]);
    };

    const handleRemoveNewFile = (index: number) => {
        const newFiles = [...filesToUpload];
        newFiles.splice(index, 1);
        setFilesToUpload(newFiles);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        try {
          const formData = new FormData();
          formData.append("title", title);
          formData.append("content", content);
          
          if (mediaToRemove.length > 0) {
            formData.append("mediaToRemove", JSON.stringify(mediaToRemove));
          }
          
          filesToUpload.forEach(file => {
            formData.append("newMedia", file);
          });
      
          const response = await axios.put(
            `http://localhost:8080/sharebox/post/update/${postId}`,
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
                Authorization: `Bearer ${sessionStorage.getItem("token")}`
              }
            }
          );
      
          router.push(`/post/${postId}`);
        } catch (error) {
          console.error("Error updating post:", error);
          alert("Failed to update post. Please check your input.");
        }
      };

    const handleCancel = () => {
        router.push(`/post/${postId}`);
    };

    if (!post) return <MainLayout>Loading...</MainLayout>;

    return (
        <MainLayout>
            <div className="w-full min-h-screen flex justify-center">
                <div className="w-[70%] mt-8">
                    <div className="flex justify-between items-center mb-6">
                        <h1 className="text-2xl font-bold">Edit Post</h1>
                        <button 
                            onClick={handleCancel}
                            className="p-2 rounded-full hover:bg-gray-200"
                        >
                            <Image src={CloseIcon} alt="Close" width={20} height={20} />
                        </button>
                    </div>
                    
                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label htmlFor="title" className="block text-lg font-medium mb-2">
                                Title
                            </label>
                            <input
                                id="title"
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                className="w-full p-3 border border-gray-300 rounded-lg"
                                required
                            />
                        </div>
                        
                        <div>
                            <label htmlFor="content" className="block text-lg font-medium mb-2">
                                Content
                            </label>
                            <textarea
                                id="content"
                                value={content}
                                onChange={(e) => setContent(e.target.value)}
                                className="w-full p-3 border border-gray-300 rounded-lg min-h-[200px]"
                                required
                            />
                        </div>
                        
                        <div>
                            <label className="block text-lg font-medium mb-2">
                                Current Media
                            </label>
                            <div className="grid grid-cols-3 gap-4">
                                {media.map((url, index) => (
                                    <div key={index} className="relative group">
                                        {url.endsWith(".mp4") || url.endsWith(".mov") ? (
                                            <video 
                                                src={url} 
                                                controls
                                                className="w-full h-40 object-cover rounded-lg"
                                            />
                                        ) : (
                                            <img 
                                                src={url} 
                                                alt={`Media ${index}`}
                                                className="w-full h-40 object-cover rounded-lg"
                                            />
                                        )}
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveMedia(url)}
                                            className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                                        >
                                            ×
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                        
                        <div>
                            <label htmlFor="newMedia" className="block text-lg font-medium mb-2">
                                Add New Media
                            </label>
                            <input
                                id="newMedia"
                                type="file"
                                multiple
                                onChange={handleFileChange}
                                className="w-full p-3 border border-gray-300 rounded-lg"
                                accept="image/*, video/*"
                            />
                            
                            <div className="grid grid-cols-3 gap-4 mt-4">
                                {filesToUpload.map((file, index) => (
                                    <div key={index} className="relative group">
                                        {file.type.startsWith("video/") ? (
                                            <video 
                                                src={URL.createObjectURL(file)} 
                                                controls
                                                className="w-full h-40 object-cover rounded-lg"
                                            />
                                        ) : (
                                            <img 
                                                src={URL.createObjectURL(file)} 
                                                alt={`New file ${index}`}
                                                className="w-full h-40 object-cover rounded-lg"
                                            />
                                        )}
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveNewFile(index)}
                                            className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                                        >
                                            ×
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                        
                        <div className="flex justify-end space-x-4">
                            <button
                                type="button"
                                onClick={handleCancel}
                                className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-100"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                            >
                                Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </MainLayout>
    );
}