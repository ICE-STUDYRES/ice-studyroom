import { useState, useEffect, useCallback } from "react";
import axios from "axios";

const usePenaltyLogic = () => {
  const [penalties, setPenalties] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [penaltyForm, setPenaltyForm] = useState({
    studentId: "",
    name: "",
    type: "",
    issueDate: new Date().toISOString().split("T")[0],
    expiryDate: "",
  });

  // ✅ 패널티 목록 불러오기
  const fetchPenalties = useCallback(async () => {
    try {
      const accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) return;

      const response = await axios.get("/api/admin/penalty", {
        headers: { Authorization: `Bearer ${accessToken}` },
      });

      if (!response.data || !Array.isArray(response.data.data)) return;

      setPenalties(
        response.data.data.map((item) => ({
          studentId: item.studentNum,
          name: item.userName,
          type: item.reason || "미정",
          issueDate: item.penaltyStart ? item.penaltyStart.split("T")[0] : "N/A",
          expiryDate: item.penaltyEnd ? item.penaltyEnd.split("T")[0] : "N/A",
          status: item.status === "VALID" ? "active" : "inactive",
        }))
      );
    } catch (error) {
      alert("❌ 패널티 목록을 불러오는 중 오류가 발생했습니다.");
    }
  }, []);

  useEffect(() => {
    fetchPenalties();
  }, [fetchPenalties]);

  const handleFormChange = (field, value) => {
    setPenaltyForm((prev) => ({ ...prev, [field]: value }));
  };

  // ✅ 패널티 추가
  const handleAddPenalty = async () => {
    if (!penaltyForm.studentId || !penaltyForm.expiryDate) {
      alert("❌ 모든 필드를 입력해주세요.");
      return;
    }

    const newPenalty = {
      studentNum: penaltyForm.studentId,
      penaltyEndAt: new Date(penaltyForm.expiryDate).toISOString(),
    };

    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      alert("❌ 로그인 후 이용해주세요.");
      return;
    }

    try {
      const response = await fetch("/api/admin/penalty", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(newPenalty),
      });

      if (!response.ok) {
        alert("❌ 패널티 추가 실패.");
        return;
      }

      alert("✅ 패널티가 추가되었습니다.");
      fetchPenalties();
      setIsModalOpen(false);
    } catch (error) {
      alert("❌ 패널티 추가 중 오류가 발생했습니다.");
    }
  };

  // ✅ 패널티 삭제
  const handleDeletePenalty = async (studentId) => {
    if (!studentId) {
      alert("❌ 삭제할 학번이 없습니다.");
      return;
    }

    const accessToken = sessionStorage.getItem("accessToken");
    if (!accessToken) {
      alert("❌ 로그인 후 이용해주세요.");
      return;
    }

    if (!window.confirm(`패널티를 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const response = await fetch("/api/admin/penalty", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ studentNum: studentId }),
      });

      if (!response.ok) {
        alert("❌ 패널티 삭제 실패.");
        return;
      }

      alert(`패널티가 삭제되었습니다.`);
      fetchPenalties();
    } catch (error) {
      alert("❌ 패널티 삭제 중 오류가 발생했습니다.");
    }
  };

  return {
    penalties,
    isModalOpen,
    setIsModalOpen,
    penaltyForm,
    setPenaltyForm,
    handleFormChange,
    handleAddPenalty,
    fetchPenalties,
    handleDeletePenalty,
  };
};

export default usePenaltyLogic;
