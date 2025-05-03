import Image from "next/image";
import ChatIcon from "../../public/comment-dots-solid.svg";
import ThumbUpIcon from "../../public/thumbs-up-solid2.svg"; 
import ThumbDownIcon from "../../public/thumbs-down-solid.svg"; 
import { useRouter } from "next/navigation";
import axios from "axios";
import { SetStateAction } from "react";

export default function NotificationCard({ 
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
    commentId?: number, // Make optional since vote notifications won't have commentId
    postId: number, 
    setRenew: (value: SetStateAction<number>) => void 
}) {
    const router = useRouter();

    // Handle notification message formats for comments and votes
    const parseNotification = () => {
        const indicators = [
            {
                text: " just commented on your post!",
                type: "comment",
                icon: ChatIcon
            },
            {
                text: " just replied to your comment!",
                type: "comment",
                icon: ChatIcon
            },
            {
                text: " just upvoted your post!",
                type: "vote",
                icon: ThumbUpIcon
            },
            {
                text: " just downvoted your post!",
                type: "vote",
                icon: ThumbDownIcon
            }
        ];

        for (const indicator of indicators) {
            const index = message.indexOf(indicator.text);
            if (index !== -1) {
                const username = message.substring(0, index);
                // For comment notifications, extract comment content
                const content = indicator.type === "comment" 
                    ? message.substring(index + indicator.text.length) 
                    : "";
                
                return {
                    message: message.substring(0, index + indicator.text.length),
                    comment: content,
                    type: indicator.type,
                    icon: indicator.icon
                };
            }
        }

        // Fallback for other notification types
        return {
            message: message,
            comment: "",
            type: "other",
            icon: ChatIcon // Default icon
        };
    };

    const { message: mes, comment, type, icon } = parseNotification();

    const handleNavigate = async() => {
        try {
            await axios.post(
                `http://localhost:8080/sharebox/noti/delete/${id}`
            );
            setRenew(n => n + 1);
            
            // Only set commentId in session storage if it exists (comment notifications)
            if (commentId) {
                sessionStorage.setItem("commentId", "id" + commentId.toString());
            }
            
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
                {type === "comment" && (
                    <div className="mt-1 flex gap-2 w-[200px]">
                        <Image
                            src={icon}
                            alt="Notification Icon"
                            className="w-[12px]"
                        />
                        <p className="whitespace-nowrap overflow-hidden text-ellipsis text-sm text-textGrayColor1">
                            {comment || "No comment available"}
                        </p>
                    </div>
                )}
                {type === "vote" && (
                    <div className="mt-1 flex gap-2 w-[200px]">
                        <Image
                            src={icon}
                            alt="Vote Icon"
                            className="w-[12px]"
                        />
                        <p className="whitespace-nowrap overflow-hidden text-ellipsis text-sm text-textGrayColor1">
                            Post vote notification
                        </p>
                    </div>
                )}
            </div>
        </div>
    )
}