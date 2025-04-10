'use client'

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import Image from "next/image";
import CloseIcon from "../../public/xmark-solid.svg";
import { Music, Game, Anime, Movie, Manga, Sport } from "./topics";
import ToastMessage from "./toastMessage";

export default function EditPost({ post, onClose, onSuccess }: { post: any, onClose: () => void, onSuccess: () => void }) {
    const router = useRouter();
    const userString = sessionStorage.getItem("user");
    const user = userString ? JSON.parse(userString) : {};
    const [title, setTitle] = useState(post.title);
    const [content, setContent] = useState(post.content);
    const [selectedTopic, setSelectedTopic] = useState(post.postTopics[0].id);
    const [selectedCommunity, setSelectedCommunity] = useState(post.communityId);
    const [communities, setCommunities] = useState<any[]>([]);
    const [currentMedia, setCurrentMedia] = useState<string[]>(post.media || []);
    const [newMedia, setNewMedia] = useState<File[]>([]);
    const [mediaToRemove, setMediaToRemove] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);
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

    useEffect(() => {
        const fetchCommunities = async () => {
            try {
                const res = await axios.get(
                    `http://localhost:8080/sharebox/community/user/${user.userId}`
                );
                if (res.data.result) {
                    setCommunities(res.data.result);
                }
            } catch (error) {
                console.error("Error fetching communities:", error);
            }
        };

        fetchCommunities();
    }, [user.userId]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            const filesArray = Array.from(e.target.files);
            setNewMedia(prev => [...prev, ...filesArray]);
        }
    };

    const handleRemoveCurrentMedia = (url: string) => {
        setCurrentMedia(currentMedia.filter(item => item !== url));
        setMediaToRemove(prev => [...prev, url]);
    };

    const handleRemoveNewMedia = (index: number) => {
        setNewMedia(prev => prev.filter((_, i) => i !== index));
    };

    const isVideo = (url: string) => {
        const videoExtensions = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];
        return videoExtensions.some(ext => url.toLowerCase().endsWith(ext));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const topicObj = {
                id: selectedTopic,
                name: getTopicName(selectedTopic)
            };

            const formData = new FormData();
            formData.append("title", title);
            formData.append("content", content);
            formData.append("postTopics[0].id", selectedTopic.toString());
            formData.append("postTopics[0].name", getTopicName(selectedTopic));
            
            if (selectedCommunity) {
                formData.append("communityId", selectedCommunity.toString());
            }

            // Add media to remove
            mediaToRemove.forEach((url, index) => {
                formData.append(`mediaToRemove[${index}]`, url);
            });

            // Add new media files
            newMedia.forEach((file, index) => {
                formData.append(`newMedia[${index}]`, file);
            });

            const token = user.token;
            const response = await axios.put(
                `http://localhost:8080/sharebox/post/update/${post.postId}`, 
                formData,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            if (response.data.result) {
                setMessage({
                    type: "success",
                    message: "Updated Post Successfully!",
                    redirect: true
                });
                setShowMessage(true);
                setTimeout(() => {
                    onSuccess();
                    onClose();
                    router.push('/');
                }, 2000);
            }
        } catch (error) {
            console.error("Error updating post:", error);
            setMessage({
                type: "warning",
                message: "Có lỗi xảy ra khi xóa bài post!",
                redirect: false
            });
            setShowMessage(true);
        } finally {
            setIsLoading(false);
        }
    };

    const getTopicName = (id: number): string => {
        switch (id) {
            case 1: return "Music";
            case 2: return "Game";
            case 3: return "Anime";
            case 4: return "Movie";
            case 5: return "Manga";
            case 6: return "Sport";
            default: return "";
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[101]">
            <div className="bg-white rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto p-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-bold">Edit Post</h2>
                    <Image 
                        src={CloseIcon}
                        alt="Close"
                        className="w-6 h-6 cursor-pointer"
                        onClick={onClose}
                    />
                </div>
                
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">Title</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">Content</label>
                        <textarea
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            className="w-full p-2 border rounded h-32 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">Topic</label>
                        <div className="flex flex-wrap gap-2">
                            <div onClick={() => setSelectedTopic(1)} className={`cursor-pointer ${selectedTopic === 1 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Music isHaveBg isSmall canHover={false}/>
                            </div>
                            <div onClick={() => setSelectedTopic(2)} className={`cursor-pointer ${selectedTopic === 2 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Game isHaveBg isSmall canHover={false}/>
                            </div>
                            <div onClick={() => setSelectedTopic(3)} className={`cursor-pointer ${selectedTopic === 3 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Anime isHaveBg isSmall canHover={false}/>
                            </div>
                            <div onClick={() => setSelectedTopic(4)} className={`cursor-pointer ${selectedTopic === 4 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Movie isHaveBg isSmall canHover={false}/>
                            </div>
                            <div onClick={() => setSelectedTopic(5)} className={`cursor-pointer ${selectedTopic === 5 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Manga isHaveBg isSmall canHover={false}/>
                            </div>
                            <div onClick={() => setSelectedTopic(6)} className={`cursor-pointer ${selectedTopic === 6 ? 'ring-2 ring-blue-500' : ''}`}>
                                <Sport isHaveBg isSmall canHover={false}/>
                            </div>
                        </div>
                    </div>
                    
                    {communities.length > 0 && (
                        <div className="mb-4">
                            <label className="block text-gray-700 mb-2">Community</label>
                            <select
                                value={selectedCommunity || ""}
                                onChange={(e) => setSelectedCommunity(e.target.value ? Number(e.target.value) : null)}
                                className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">Không có cộng đồng</option>
                                {communities.map((community) => (
                                    <option key={community.communityId} value={community.communityId}>
                                        {community.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}
                    
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">Media To Remove</label>
                        <div className="flex flex-wrap gap-2">
                            {currentMedia.length === 0 ? (
                                <p className="text-gray-500">Don't have media yet!</p>
                            ) : (
                                currentMedia.map((url, index) => (
                                    <div key={index} className="relative group">
                                        {isVideo(url) ? (
                                            <video 
                                                src={url} 
                                                className="w-20 h-20 object-cover rounded" 
                                            />
                                        ) : (
                                            <img 
                                                src={url} 
                                                alt={`Media ${index}`} 
                                                className="w-20 h-20 object-cover rounded" 
                                            />
                                        )}
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveCurrentMedia(url)}
                                            className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center opacity-0 group-hover:opacity-100"
                                        >
                                            ×
                                        </button>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                    
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">Add New Media</label>
                        <div className="flex flex-wrap gap-2 mb-2">
                            {newMedia.map((file, index) => (
                                <div key={index} className="relative group">
                                    <img 
                                        src={URL.createObjectURL(file)} 
                                        alt={`New Media ${index}`} 
                                        className="w-20 h-20 object-cover rounded" 
                                    />
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveNewMedia(index)}
                                        className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center opacity-0 group-hover:opacity-100"
                                    >
                                        ×
                                    </button>
                                </div>
                            ))}
                        </div>
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            multiple
                            accept="image/*,video/*"
                            className="hidden"
                        />
                        <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                        >
                            Choose files
                        </button>
                    </div>
                    
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
                            disabled={isLoading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
                            disabled={isLoading}
                        >
                            {isLoading ? "Loading..." : "Save update"}
                        </button>
                    </div>
                </form>
            </div>
            {showMessage ? <ToastMessage type={message.type} message={message.message} redirect={message.redirect} setShowMessage={setShowMessage} position="top-right"/> : <></>}
        </div>
    );
}