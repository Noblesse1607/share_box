'use client'
import Image from "next/image";
import loginImage from '../../../public/login_image.svg';
import loginImage2 from '../../../public/login_image_2.svg';
import gmailIcon from '../../../public/envelope-regular.svg';
import warningIcon from "../../../public/triangle-exclamation-solid.svg";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import ToastMessage from "../../components/toastMessage";
import { emailValid } from "../../../validation/validation";
import axios from "axios";

export default function ForgotPassword() {
    const router = useRouter();
    const [step, setStep] = useState<number>(1); // 1: Email entry, 2: Verification code, 3: New password
    const [errors, setErrors] = useState<{
      email: string,
      verificationCode: string,
      newPassword: string,
      confirmPassword: string
    }>({
      email: "",
      verificationCode: "",
      newPassword: "",
      confirmPassword: ""
    });
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
    const [data, setData] = useState<{
      email: string,
      verificationCode: string,
      newPassword: string,
      confirmPassword: string
    }>({
      email: "",
      verificationCode: "",
      newPassword: "",
      confirmPassword: ""
    });

    const handleChange = (e: any) => {
      const { name, value } = e.target;
      setData((prev: any) => ({
        ...prev,
        [name]: value,
      }));

      // Validate based on field
      if (name === "email") {
        const validationError = emailValid(value);
        setErrors(prev => ({
          ...prev,
          email: validationError || ""
        }));
      } else if (name === "verificationCode") {
        setErrors(prev => ({
          ...prev,
          verificationCode: value.length !== 6 ? "Verification code must be 6 digits" : ""
        }));
      } else if (name === "newPassword") {
        setErrors(prev => ({
          ...prev,
          newPassword: value.length < 8 ? "Password must be at least 8 characters" : "",
          confirmPassword: value !== data.confirmPassword && data.confirmPassword !== "" ? "Passwords do not match" : ""
        }));
      } else if (name === "confirmPassword") {
        setErrors(prev => ({
          ...prev,
          confirmPassword: value !== data.newPassword ? "Passwords do not match" : ""
        }));
      }
    }

    const requestVerificationCode = async () => {
      if (!errors.email && data.email) {
        try {
            console.log("Before API call, step:", step);
          // Send request to backend to generate and send verification code via MailTrap
          const res = await axios.post(
            `http://localhost:8080/sharebox/forgot-password/request`,
            {
              "email": data.email
            }
          );

          console.log("API response:", res.data);
          
          if (res.data.code === 1000) {
            setMessage({
              type: "success",
              message: "Verification code sent to your email!",
              redirect: false
            });
            setShowMessage(true);
            setStep(2); // Move to verification code step
            console.log("After success, step set to:", 2);
          }
        } catch (error) {
          setMessage({
            type: "warning",
            message: "Error sending verification code",
            redirect: false
          });
          setShowMessage(true);
        }
      } else {
        setMessage({
          type: "warning",
          message: "Please enter a valid email",
          redirect: false
        });
        setShowMessage(true);
      }
    }

    const verifyCode = async () => {
      if (!errors.verificationCode && data.verificationCode) {
        try {
          // Verify the code with backend
          const res = await axios.post(
            `http://localhost:8080/sharebox/forgot-password/verify`,
            {
              "email": data.email,
              "verificationCode": data.verificationCode
            }
          );
          
          if (res.data.code === 1000) {
            setMessage({
              type: "success",
              message: "Code verified successfully!",
              redirect: false
            });
            setShowMessage(true);
            setStep(3); // Move to new password step
          }
        } catch (error) {
          setMessage({
            type: "warning",
            message: "Invalid verification code",
            redirect: false
          });
          setShowMessage(true);
        }
      } else {
        setMessage({
          type: "warning",
          message: "Please enter the 6-digit verification code",
          redirect: false
        });
        setShowMessage(true);
      }
    }

    const resetPassword = async () => {
      if (!errors.newPassword && !errors.confirmPassword && data.newPassword && data.confirmPassword) {
        try {
          // Reset password with backend
          const res = await axios.post(
            `http://localhost:8080/sharebox/forgot-password/reset`,
            {
              "email": data.email,
              "verificationCode": data.verificationCode,
              "newPassword": data.newPassword
            }
          );
          
          if (res.data.code === 1000) {
            setMessage({
              type: "success",
              message: "Password reset successfully!",
              redirect: true
            });
            setShowMessage(true);
            setTimeout(() => {
              router.push('/login');
            }, 2000);
          }
        } catch (error) {
          setMessage({
            type: "warning",
            message: "Error resetting password",
            redirect: false
          });
          setShowMessage(true);
        }
      } else {
        setMessage({
          type: "warning",
          message: "Please enter matching passwords",
          redirect: false
        });
        setShowMessage(true);
      }
    }

    const renderStepContent = () => {
      switch(step) {
        case 1:
          return (
            <>
              <div className="mt-8 w-[80%]">
                <div>
                  <p className="text-xl select-none">Email</p>
                  <div className="w-full h-[60px] mt-2 rounded-2xl border-2 border-textGrayColor2 flex p-2 focus-within:border-inputBorderColor">
                    <div className="w-[60px] h-full flex items-center justify-center mr-2">
                      <Image 
                        src={gmailIcon}
                        alt="Gmail Icon"
                        className="w-[25px]"
                      />
                    </div>
                    <div className="w-[3px] h-full bg-textGrayColor2 mr-3"></div>
                    <input 
                      onChange={handleChange} 
                      name="email" 
                      type="email" 
                      className="w-[85%] h-full text-xl outline-none" 
                      placeholder="Enter your registered email"
                    />
                  </div>
                  {errors.email && 
                    <div className="ml-7 mt-2 flex gap-2 items-center">
                      <Image 
                        src={warningIcon}
                        alt="Warning Icon"
                        className="w-[20px]"
                      />
                      <p className="text-[16px] text-warningMessageBackground font-bold">{errors.email}</p>
                    </div>
                  }
                </div>
              </div>

              <button 
                onClick={requestVerificationCode} 
                className="mt-[64px] w-[80%] h-[70px] bg-buttonColor rounded-xl text-white font-bold text-2xl hover:scale-[1.01] ease-linear duration-100 shadow-lg"
              >
                Request Verification Code
              </button>
            </>
          );
        case 2:
          return (
            <>
              <div className="mt-8 w-[80%]">
                <div>
                  <p className="text-xl select-none">Verification Code</p>
                  <div className="w-full h-[60px] mt-2 rounded-2xl border-2 border-textGrayColor2 flex p-2 focus-within:border-inputBorderColor">
                    <input 
                      onChange={handleChange} 
                      name="verificationCode" 
                      type="text" 
                      className="w-full h-full text-xl outline-none text-center tracking-wider" 
                      placeholder="Enter 6-digit code"
                      maxLength={6}
                    />
                  </div>
                  {errors.verificationCode && 
                    <div className="ml-7 mt-2 flex gap-2 items-center">
                      <Image 
                        src={warningIcon}
                        alt="Warning Icon"
                        className="w-[20px]"
                      />
                      <p className="text-[16px] text-warningMessageBackground font-bold">{errors.verificationCode}</p>
                    </div>
                  }
                </div>
              </div>

              <button 
                onClick={verifyCode} 
                className="mt-[64px] w-[80%] h-[70px] bg-buttonColor rounded-xl text-white font-bold text-2xl hover:scale-[1.01] ease-linear duration-100 shadow-lg"
              >
                Verify Code
              </button>
              
              <button 
                onClick={() => setStep(1)} 
                className="mt-4 w-[80%] h-[50px] border border-textGrayColor2 rounded-xl text-textGrayColor1 font-bold text-xl hover:scale-[1.01] ease-linear duration-100"
              >
                Back to Email
              </button>
            </>
          );
        case 3:
          return (
            <>
              <div className="mt-8 w-[80%]">
                <div>
                  <p className="text-xl select-none">New Password</p>
                  <div className="w-full h-[60px] mt-2 rounded-2xl border-2 border-textGrayColor2 flex p-2 focus-within:border-inputBorderColor">
                    <input 
                      onChange={handleChange} 
                      name="newPassword" 
                      type="password" 
                      className="w-full h-full text-xl outline-none" 
                      placeholder="Enter new password"
                    />
                  </div>
                  {errors.newPassword && 
                    <div className="ml-7 mt-2 flex gap-2 items-center">
                      <Image 
                        src={warningIcon}
                        alt="Warning Icon"
                        className="w-[20px]"
                      />
                      <p className="text-[16px] text-warningMessageBackground font-bold">{errors.newPassword}</p>
                    </div>
                  }
                </div>
                
                <div className="mt-6">
                  <p className="text-xl select-none">Confirm Password</p>
                  <div className="w-full h-[60px] mt-2 rounded-2xl border-2 border-textGrayColor2 flex p-2 focus-within:border-inputBorderColor">
                    <input 
                      onChange={handleChange} 
                      name="confirmPassword" 
                      type="password" 
                      className="w-full h-full text-xl outline-none" 
                      placeholder="Confirm new password"
                    />
                  </div>
                  {errors.confirmPassword && 
                    <div className="ml-7 mt-2 flex gap-2 items-center">
                      <Image 
                        src={warningIcon}
                        alt="Warning Icon"
                        className="w-[20px]"
                      />
                      <p className="text-[16px] text-warningMessageBackground font-bold">{errors.confirmPassword}</p>
                    </div>
                  }
                </div>
              </div>

              <button 
                onClick={resetPassword} 
                className="mt-[64px] w-[80%] h-[70px] bg-buttonColor rounded-xl text-white font-bold text-2xl hover:scale-[1.01] ease-linear duration-100 shadow-lg"
              >
                Reset Password
              </button>
              
              <button 
                onClick={() => setStep(2)} 
                className="mt-4 w-[80%] h-[50px] border border-textGrayColor2 rounded-xl text-textGrayColor1 font-bold text-xl hover:scale-[1.01] ease-linear duration-100"
              >
                Back to Verification
              </button>
            </>
          );
        default:
          return null;
      }
    }

    return (
      <main className="relative w-full h-[100vh] flex items-center py-[64px] pl-[64px] overflow-hidden">
        <title>Share box | Forgot Password</title>
        <section className="w-[50%] h-[650px] bg-imageBackground relative rounded-2xl">
          <Image 
            src={loginImage}
            alt="Login Image"
            className="absolute w-[80%] left-[-50px] top-[100px]"
          />
          <Image 
            src={loginImage2}
            alt="Disc Image"
            className="absolute w-[60%] right-[-140px] top-[-40px]"
          />
        </section>
        
        <section className="w-[50%] h-[650px] flex flex-col pt-[64px] pl-[128px] pr-[64px] items-center">
          <h1 className="text-4xl font-bold text-center select-none">FORGOT PASSWORD</h1>
          <p className="text-textGrayColor1 mt-2 text-center max-w-[80%]">
            {step === 1 && "Enter your email to receive a verification code"}
            {step === 2 && "Enter the 6-digit code sent to your email"}
            {step === 3 && "Create a new password for your account"}
          </p>
          
          {renderStepContent()}
          
          <p className="mt-8 font-semibold select-none">
            Remember your password? <Link href="/login" className="text-textGrayColor1 hover:underline underline-offset-4">Login here</Link>
          </p>
        </section>
        
        {showMessage ? 
          <ToastMessage 
            type={message.type} 
            message={message.message} 
            redirect={message.redirect} 
            setShowMessage={setShowMessage}
          /> 
          : <></>
        }
      </main>
    );
}