const categoryButtons = [
  { id: "RESERVATION", name: "예약" },
  { id: "CHECKIN_QR", name: "체크인(QR)" },
  { id: "EXTEND", name: "연장" },
  { id: "CANCEL_CHANGE", name: "취소 / 변경" },
  { id: "RULES", name: "이용시간 / 규정" },
  { id: "PENALTY", name: "패널티 / 제재" },
  { id: "FACILTY", name: "시설 / 장비" },
  { id: "ETC", name: "기타" },
];

const ChatbotButtons = ({ categories, onSelect }) => (
    <div className="grid grid-cols-2 gap-3 mt-6 mb-4">
      {categories.map((cat) => (
        <button
          key={cat.id}
          onClick={() => onSelect(cat)}
          className="faq-button"
        >
          {cat.name}
        </button>
      ))}
    </div>
  );

export default ChatbotButtons;