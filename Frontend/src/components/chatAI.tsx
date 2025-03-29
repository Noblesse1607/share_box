import { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';

const AiChat = () => {
  const [isOpen, setIsOpen] = useState(false);
  
  interface Message {
    id: number;
    text: string;
    sender: 'user' | 'ai';
  }

  // Remove /sharebox from the URL path
  const BACKEND_URL = 'http://localhost:8080/sharebox';
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string>('');
  const userString = sessionStorage.getItem("user");    
  const user = userString ? JSON.parse(userString) : {};
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Get userId from sessionStorage
    console.log('User Id: ', user.userId);
    
    // Use user.userId if available, or generate a fallback ID
    const userIdToUse = user.userId || uuidv4();
    
    // Create a fixed session ID format with the userId as the unique identifier
    // This ensures the same session ID is used every time the user logs in
    const fixedSessionId = `user_${userIdToUse}_chat_history`;
    
    setSessionId(fixedSessionId);
    localStorage.setItem('ai_chat_session_id', fixedSessionId);
    
    // Load chat history with the fixed session ID
    loadChatHistory(fixedSessionId, userIdToUse).catch(console.error);
  }, []);

  const loadChatHistory = async (sid: string, uid: string) => {
    try {
      console.log(`Loading chat history for session ${sid} with user ${uid}`);
      
      const response = await axios.get(`${BACKEND_URL}/ai-chat/history/${sid}`, {
        headers: {
          'User-Id': uid
        },
        validateStatus: (status) => status === 200 || status === 404 || status === 403
      });
      
      if (response.status === 403) {
        console.warn('Session validation failed. Creating a new session.');
        // If session validation fails, still use the fixed format but regenerate it
        const fixedSessionId = `user_${uid}_chat_history`;
        setSessionId(fixedSessionId);
        localStorage.setItem('ai_chat_session_id', fixedSessionId);
        return;
      }
      
      // Only process if response has data and it's not a 404
      if (response.status === 200 && response.data && Array.isArray(response.data)) {
        const formattedMessages: Message[] = [];
        
        // Process the history as pairs of user/model messages
        response.data.forEach((message: any) => {
          if (message.role === 'user') {
            formattedMessages.push({
              id: Date.now() + Math.random(),
              text: message.content,
              sender: 'user'
            });
          } else if (message.role === 'model') {
            formattedMessages.push({
              id: Date.now() + Math.random(),
              text: message.content,
              sender: 'ai'
            });
          }
        });
        
        setMessages(formattedMessages);
      }
      // Silently handle 404 - it means no history exists yet
    } catch (error) {
      console.error('Error loading chat history:', error);
      // Don't show error to user for history loading
    }
  };

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

    const userMessage: Message = {
      id: Date.now(),
      text: inputValue,
      sender: 'user',
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      console.log(`Sending message with session ${sessionId} and user ${user.userId}`);
      
      const response = await axios.post(
        `${BACKEND_URL}/ai-chat/ask`, 
        inputValue.trim(), 
        {
          headers: {
            'Content-Type': 'text/plain',
            'Session-Id': sessionId,
            'User-Id': user.userId
          },
        }
      );

      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          text: response.data || 'Sorry, I couldn\'t process your request.',
          sender: 'ai',
        },
      ]);
    } catch (error: any) {
      console.error('Error:', error);
      
      // Check if it's a 403 error (session validation failed)
      if (error.response && error.response.status === 403) {
        // Create a new session with the same fixed format
        const fixedSessionId = `user_${user.userId}_chat_history`;
        setSessionId(fixedSessionId);
        localStorage.setItem('ai_chat_session_id', fixedSessionId);
        
        setMessages((prev) => [
          ...prev,
          {
            id: Date.now() + 1,
            text: 'Your session has been refreshed. Please try sending your message again.',
            sender: 'ai',
          },
        ]);
      } else {
        setMessages((prev) => [
          ...prev,
          {
            id: Date.now() + 1,
            text: 'Sorry, there was an error processing your request.',
            sender: 'ai',
          },
        ]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const clearChat = async () => {
    try {
      await axios.delete(`${BACKEND_URL}/ai-chat/history/${sessionId}`, {
        headers: {
          'User-Id': user.userId
        }
      });
      setMessages([]);
    } catch (error) {
      console.error('Error clearing chat history:', error);
    }
  };

  return (
    <div className="fixed bottom-4 left-4 z-50">
      {/* Chat toggle button */}
      <button
        onClick={toggleChat}
        className="bg-mainColor hover:bg-opacity-90 text-white rounded-full w-14 h-14 flex items-center justify-center shadow-lg focus:outline-none transition-all duration-300"
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
        <div className="absolute bottom-16 left-0 w-80 md:w-96 bg-white rounded-lg shadow-xl overflow-hidden animate-slide-in-left">
          {/* Chat header */}
          <div className="bg-mainColor text-white p-4 flex justify-between items-center">
            <h3 className="font-medium">Share Box AI Assistant</h3>
            <div className="flex">
              {/* Clear chat button */}
              {messages.length > 0 && (
                <button 
                  onClick={clearChat} 
                  className="text-white focus:outline-none hover:text-gray-200 mr-3" 
                  aria-label="Clear chat"
                  title="Clear chat history"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-5 h-5">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              )}
              {/* Close button */}
              <button onClick={toggleChat} className="text-white focus:outline-none hover:text-gray-200" aria-label="Close chat">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" className="w-5 h-5">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          {/* Chat messages */}
          <div className="p-4 h-80 overflow-y-auto">
            {messages.length === 0 ? (
              <div className="text-center text-gray-500 mt-10">
                <p>How can I help you today?</p>
                <p className="text-xs mt-2">Powered by AI Assistant</p>
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
                className="bg-mainColor hover:bg-opacity-90 text-white px-4 py-2 rounded-r-lg focus:outline-none disabled:opacity-50 transition-all duration-300"
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