import Image from "next/image";
import ChatIcon from "../../public/comment-dots-solid.svg";
import { useRouter } from "next/navigation";
import axios from "axios";
import { SetStateAction } from "react";

export default function CommentNotiCard({ 
    id, 
    message, 
    image, 
    commentId, 
    postId, 
    setRenew 
}: { 
    id: number, 
    message: string, 
    image: string, 
    commentId: number, 
    postId: number, 
    setRenew: (value: SetStateAction<number>) => void 
}) {
    const router = useRouter();

    // Handle two possible notification formats:
    // 1. "Username just commented on your post!Content"
    // 2. "Username just replied to your comment!Content"
    const handleMessageParsing = () => {
        const commentIndicators = [
            " just commented on your post!",
            " just replied to your comment!"
        ];

        for (const indicator of commentIndicators) {
            const index = message.indexOf(indicator);
            if (index !== -1) {
                const username = message.substring(0, index);
                const content = message.substring(index + indicator.length);
                return {
                    message: message.substring(0, index + indicator.length),
                    comment: content
                };
            }
        }

        // Fallback parsing if no standard format is found
        return {
            message: message,
            comment: ""
        };
    };

    const { message: mes, comment } = handleMessageParsing();

    const handleNavigate = async() => {
        try {
            await axios.post(
                `http://localhost:8080/sharebox/noti/delete/${id}`
            );
            setRenew(n => n + 1);
            sessionStorage.setItem("commentId", "id" + commentId.toString());
            router.push(`/post/${postId}`);
        } catch (error) {
            console.error("Error deleting notification:", error);
            router.push(`/post/${postId}`);
        }
    }

    return (
        <div 
            onClick={handleNavigate} 
            className="w-full p-4 flex items-center hover:bg-slate-100 duration-150 cursor-pointer rounded-md"
        > 
            <div className="w-[60px] h-[60px] rounded-full overflow-hidden">
                <img 
                    src={image} 
                    alt="Avatar" 
                    className="w-full h-full object-cover"
                />
            </div>

            <div className="ml-4 w-[250px] break-words">
                <h3 className="text-textHeadingColor font-bold">{mes}</h3>
                <div className="mt-1 flex gap-2 w-[200px]">
                    <Image
                        src={ChatIcon}
                        alt="Chat Icon"
                        className="w-[12px]"
                    />
                    <p className="whitespace-nowrap overflow-hidden text-ellipsis text-sm text-textGrayColor1">
                        {comment || "No comment available"}
                    </p>
                </div>
            </div>
        </div>
    )
}