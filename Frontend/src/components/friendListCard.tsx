import Image from "next/image";
import { useRouter } from "next/navigation";

export default function FriendListCard({ friend }: { friend: any }) {
    const router = useRouter();
    return (
        <div 
            onClick={() => router.push(`/account/${friend.userId}`)} 
            className="flex p-6 gap-4 items-center rounded-lg shadow-xl border border-slate-200 hover:scale-[1.03] duration-150 cursor-pointer w-full md:w-[400px] h-[100px]"
        >
            <Image
                src={friend.avatar}
                alt="Friend Avatar"
                width={50}
                height={50}
                className="rounded-full flex-shrink-0"
            />
            <div className="overflow-hidden">
                <h3 className="font-bold text-lg truncate">{friend.username}</h3>
            </div>
        </div>
    )
}