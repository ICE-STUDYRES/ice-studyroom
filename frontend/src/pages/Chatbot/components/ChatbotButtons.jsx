import CommonButton from "./CommonButton";

const ChatbotButtons = ({ categories, onSelect }) => (
  <div className="grid grid-cols-2 gap-3 mt-6 mb-4">
    {categories.map((cat) => (
      <CommonButton
        key={cat.id}
        onClick={() => onSelect(cat)}
      >
        {cat.name}
      </CommonButton>
    ))}
  </div>
);

export default ChatbotButtons;