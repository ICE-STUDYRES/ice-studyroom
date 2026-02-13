// 공통 버튼 컴포넌트
const CommonButton = ({
  children,
  onClick,
  variant = "outline", // outline | filled | ghost
  className = "",
}) => {
  const base =
    "text-sm px-4 py-2 rounded-xl transition font-medium";

  const styles = {
    outline:
      "border border-gray-300 bg-white hover:bg-gray-50",
    filled:
      "bg-[#C4E2ED] hover:bg-[#b3d6e3]",
    ghost:
      "bg-gray-100 hover:bg-gray-200",
  };

  return (
    <button
      onClick={onClick}
      className={`${base} ${styles[variant]} ${className}`}
    >
      {children}
    </button>
  );
};

export default CommonButton;
