import Image from "next/image";
import { useRouter } from "next/navigation";

export default function UserCard({ user }: { user: any }) {
    const router = useRouter();
    return (
        <div 
            onClick={() => router.push(`/account/${user.userId}`)} 
            className="flex p-6 gap-4 items-center rounded-lg shadow-xl border border-slate-200 hover:scale-[1.03] duration-150 cursor-pointer w-full md:w-[400px] h-[100px]"
        >
            <Image
                src={user.avatar}
                alt="Friend Avatar"
                width={50}
                height={50}
                className="rounded-full flex-shrink-0 object-cover w-[50px] h-[50px]"
            />
            <div className="overflow-hidden">
                <h3 className="font-bold text-lg truncate">{user.username}</h3>
            </div>
        </div>
    )
}