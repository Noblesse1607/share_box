import axios from "axios";
import { SetStateAction, useState } from "react";

export default function CommunityRequestCard({ request, setRefresh, ownerId }: { 
  request: any, 
  setRefresh: (value: SetStateAction<number>) => void, 
  ownerId: number 
}) {
    const [status, setStatus] = useState<"PENDING" | "ACCEPTED" | "REJECTED">("PENDING");
    
    const handleAcceptRequest = async() => {
        try {
            await axios.post(
                `http://localhost:8080/sharebox/community/request/${request.id}/respond?status=ACCEPTED&ownerId=${ownerId}`
            );
            setStatus("ACCEPTED");
            setTimeout(() => {
                setRefresh(n=>n+1);
            }, 2000);
        } catch (error) {
            console.error("Error accepting community request:", error);
        }
    };
    
    const handleRejectRequest = async() => {
        try {
            await axios.post(
                `http://localhost:8080/sharebox/community/request/${request.id}/respond?status=REJECTED&ownerId=${ownerId}`
            );
            setStatus("REJECTED");
            setTimeout(() => {
                setRefresh(n=>n+1);
            }, 2000);
        } catch (error) {
            console.error("Error rejecting community request:", error);
        }
    };
    
    return (
        <div className="w-full p-4 flex items-center">
            <div className="w-[60px] h-[60px] rounded-full overflow-hidden">
                <img src={request.requester.avatar} alt="Avatar" className="w-full h-full object-cover"/>
            </div>
            <div className="ml-6">
                <h3 className="text-textHeadingColor font-bold">{request.requester.username}</h3>
                <p className="text-sm text-textGrayColor1">wants to join {request.community.name}</p>
                {status === "PENDING" &&
                    <div className="flex gap-3 mt-2">
                        <button onClick={handleAcceptRequest} className="w-[100px] p-1 text-sm bg-mainColor rounded-lg text-white hover:scale-[1.03] duration-150">
                            Accept
                        </button>
                        <button onClick={handleRejectRequest} className="w-[100px] bg-voteDownColor rounded-lg text-white hover:scale-[1.03] duration-150">
                            Decline
                        </button>
                    </div>
                }
                
                {status === "ACCEPTED" && 
                    <p className="text-sm text-textGrayColor1">User has been added to your community.</p>
                }
                
                {status === "REJECTED" &&
                    <p className="text-sm text-textGrayColor1">You have declined this join request.</p>
                }
            </div>
        </div>
    );
}