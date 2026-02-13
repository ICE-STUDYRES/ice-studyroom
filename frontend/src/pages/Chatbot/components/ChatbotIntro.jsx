import ChatMessage from "./ChatMessage";

const ChatbotIntro = () => {
  return (
    <>
      <ChatMessage>
        안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.
      </ChatMessage>

      <ChatMessage>
        궁금한 내용을 선택하시면 바로 안내해드릴게요!
      </ChatMessage>
    </>
  );
};

export default ChatbotIntro;
