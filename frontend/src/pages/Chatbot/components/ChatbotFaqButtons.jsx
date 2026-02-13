const ChatbotFaqButtons = ({ faqs, categoryId, onSelectFaq }) => {
  return (
    <div className="grid grid-cols-1 gap-3">
      {faqs.map((faq) => (
        <button
          key={faq.id}
          onClick={() =>
            onSelectFaq({
              categoryId,
              questionId: faq.id,
              text: faq.text,
            })
          }
          className="group flex items-center justify-between
                     border border-gray-300 rounded-xl
                     px-4 py-2
                     text-sm font-medium
                     bg-white
                     hover:bg-gray-50
                     transition"
        >
          {/* 질문 텍스트 */}
          <span className="flex-1 text-left text-gray-800 break-words whitespace-normal">
            {faq.text}
          </span>

          {/* 오른쪽 화살표 */}
          <span
            className="ml-3 shrink-0 text-gray-300 text-lg
                       group-hover:text-gray-500
                       transition"
          >
            ›
          </span>
        </button>
      ))}
    </div>
  );
};

export default ChatbotFaqButtons;
