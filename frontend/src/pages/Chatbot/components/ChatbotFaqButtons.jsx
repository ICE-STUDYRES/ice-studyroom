const ChatbotFaqButtons = ({ faqs, categoryId, onSelectFaq }) => {
  return (
    <div className="grid grid-cols-1 gap-3 mt-4">
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
          className="border border-gray-300 rounded-xl py-3 text-sm font-medium bg-white hover:bg-gray-50 transition"
        >
          {faq.text}
        </button>
      ))}
    </div>
  );
};

export default ChatbotFaqButtons;