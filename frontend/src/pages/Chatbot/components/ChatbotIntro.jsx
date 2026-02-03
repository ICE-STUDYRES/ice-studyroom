import ChatMessage from "./ChatMessage";

const ChatbotIntro = () => {
  return (
    <>
      <ChatMessage>
        안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.
      </ChatMessage>

      <ChatMessage>
        문의 내용을 하단에 입력하거나 아래의 선택지 중 하나를 클릭해주세요.
      </ChatMessage>
    </>
  );
};

export default ChatbotIntro;
