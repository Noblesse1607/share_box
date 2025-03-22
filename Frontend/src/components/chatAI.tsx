import { useState, useRef, useEffect } from 'react';
import axios from 'axios';

const AiChat = () => {
  const [isOpen, setIsOpen] = useState(false);
  interface Message {
    id: number;
    text: string;
    sender: 'user' | 'ai';
  }

  // Define backend URL - adjust this to your Spring Boot server address
  const BACKEND_URL = 'http://localhost:8080'; // Assuming Spring runs on port 8080

  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    // Add user message
    const userMessage: Message = {
      id: Date.now(),
      text: inputValue,
      sender: 'user',
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      // Using axios with the full backend URL
      const response = await axios.post(`${BACKEND_URL}/sharebox/ai-chat/ask`, userMessage.text, {
        headers: {
          'Content-Type': 'text/plain',
        },
      });

      // Add AI response
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          text: response.data || 'Sorry, I couldn\'t process your request.',
          sender: 'ai',
        },
      ]);
    } catch (error) {
      console.error('Error:', error);
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          text: 'Sorry, there was an error processing your request. Please check if the backend server is running.',
          sender: 'ai',
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed bottom-4 right-4 z-50">
      {/* Chat toggle button */}
      <button
        onClick={toggleChat}
        className="bg-mainColor hover:bg-mainColor text-white rounded-full w-14 h-14 flex items-center justify-center shadow-lg focus:outline-none"
        aria-label="Toggle chat"
      >
        {isOpen ? (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        ) : (
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-6 h-6">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
        )}
      </button>

      {/* Chat widget */}
      {isOpen && (
        <div className="absolute bottom-16 right-0 w-80 md:w-96 bg-white rounded-lg shadow-xl overflow-hidden">
          {/* Chat header */}
          <div className="bg-mainColor text-white p-4 flex justify-between items-center">
            <h3 className="font-medium">Share Box AI Assistant</h3>
            <button onClick={toggleChat} className="text-white focus:outline-none" aria-label="Close chat">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-5 h-5">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          {/* Chat messages */}
          <div className="p-4 h-80 overflow-y-auto">
            {messages.length === 0 ? (
              <div className="text-center text-gray-500 mt-10">
                <p>How can I help you today?</p>
                <p className="text-xs mt-2">Powered by Google Gemini</p>
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className={`mb-3 ${
                    message.sender === 'user' ? 'text-right' : 'text-left'
                  }`}
                >
                  <div
                    className={`inline-block px-4 py-2 rounded-lg max-w-[85%] ${
                      message.sender === 'user'
                        ? 'bg-mainColor text-white'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {message.text}
                  </div>
                </div>
              ))
            )}
            {isLoading && (
              <div className="text-left mb-3">
                <div className="inline-block px-4 py-2 rounded-lg bg-gray-100 text-gray-800">
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Chat input */}
          <form onSubmit={handleSubmit} className="border-t p-4">
            <div className="flex">
              <input
                type="text"
                value={inputValue}
                onChange={handleInputChange}
                placeholder="Type a message..."
                className="flex-grow px-4 py-2 border rounded-l-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                disabled={isLoading}
              />
              <button
                type="submit"
                className="bg-mainColor hover:bg-mainColor text-white px-4 py-2 rounded-r-lg focus:outline-none disabled:opacity-50"
                disabled={isLoading || !inputValue.trim()}
                aria-label="Send message"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-5 h-5">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                </svg>
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default AiChat;