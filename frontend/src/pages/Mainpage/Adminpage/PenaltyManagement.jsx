import React, { useState } from 'react';
import { X } from 'lucide-react';

const PENALTY_TYPES = {
  CANCEL: {
    label: '취소',
    days: 2,
    bgColor: 'bg-orange-100',
    textColor: 'text-orange-700'
  },
  LATE: {
    label: '지각',
    days: 3,
    bgColor: 'bg-yellow-100',
    textColor: 'text-yellow-700'
  },
  NO_SHOW: {
    label: '노쇼',
    days: 7,
    bgColor: 'bg-red-100',
    textColor: 'text-red-700'
  }
};

const PenaltyManagement = () => {
  const [penalties, setPenalties] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPenalty, setSelectedPenalty] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [penaltyForm, setPenaltyForm] = useState({
    id: '',
    studentId: '',
    name: '',
    type: '',
    issueDate: '',
    expiryDate: '',
    status: 'active'
  });

  const filteredPenalties = penalties.filter(penalty => {
    const matchesSearch = 
      penalty.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      penalty.studentId.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesStatus = 
      statusFilter === 'all' || 
      (statusFilter === 'active' && penalty.status === 'active') ||
      (statusFilter === 'inactive' && penalty.status === 'inactive');

    return matchesSearch && matchesStatus;
  });

  const calculateExpiryDate = (issueDate, type) => {
    if (!issueDate || !type) return '';
    const date = new Date(issueDate);
    date.setDate(date.getDate() + PENALTY_TYPES[type].days);
    return date.toISOString().split('T')[0];
  };

  const handleAddPenalty = () => {
    const newPenalty = {
      ...penaltyForm,
      id: Date.now().toString(),
      status: 'active'
    };
    setPenalties([...penalties, newPenalty]);
    setIsModalOpen(false);
    resetForm();
  };

  const handleUpdatePenalty = () => {
    const updatedPenalties = penalties.map(penalty =>
      penalty.id === penaltyForm.id ? { ...penaltyForm } : penalty
    );
    setPenalties(updatedPenalties);
    setIsModalOpen(false);
    resetForm();
  };

  const handleDeletePenalty = () => {
    if (window.confirm('패널티를 삭제하시겠습니까?')) {
      const updatedPenalties = penalties.filter(
        penalty => penalty.id !== penaltyForm.id
      );
      setPenalties(updatedPenalties);
      setIsModalOpen(false);
      resetForm();
    }
  };

  const resetForm = () => {
    setPenaltyForm({
      id: '',
      studentId: '',
      name: '',
      type: '',
      issueDate: '',
      expiryDate: '',
      status: 'active'
    });
    setSelectedPenalty(null);
  };

  const handleManageClick = (penalty) => {
    setSelectedPenalty(penalty);
    setPenaltyForm({ ...penalty });
    setIsModalOpen(true);
  };

  const handleNewPenalty = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const handleFormChange = (field, value) => {
    setPenaltyForm(prev => {
      const updated = { ...prev, [field]: value };
      
      if (field === 'type' || field === 'issueDate') {
        updated.expiryDate = calculateExpiryDate(
          field === 'issueDate' ? value : prev.issueDate,
          field === 'type' ? value : prev.type
        );
      }
      
      return updated;
    });
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold text-gray-900">패널티 관리</h2>
        <button 
          onClick={handleNewPenalty}
          className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors"
        >
          새 패널티 부여
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-4 border-b border-gray-200">
          <div className="flex gap-4">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="학번 또는 이름으로 검색"
              className="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm"
            />
            <select 
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-gray-200 rounded-lg text-sm"
            >
              <option value="all">전체 상태</option>
              <option value="active">활성</option>
              <option value="inactive">비활성</option>
            </select>
          </div>
        </div>

        <table className="w-full">
          <thead className="bg-gray-50 text-sm font-medium text-gray-600">
            <tr>
              <th className="text-left p-4">학생 정보</th>
              <th className="text-left p-4">패널티 종류</th>
              <th className="text-left p-4">부여 날짜</th>
              <th className="text-left p-4">만료일</th>
              <th className="text-left p-4">상태</th>
              <th className="text-right p-4">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {filteredPenalties.map((penalty) => (
              <tr key={penalty.id} className="text-sm">
                <td className="p-4">
                  <div className="font-medium text-gray-900">{penalty.name}</div>
                  <div className="text-gray-500">{penalty.studentId}</div>
                </td>
                <td className="p-4">
                  <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${
                    PENALTY_TYPES[penalty.type]?.bgColor || 'bg-gray-100'
                  } ${PENALTY_TYPES[penalty.type]?.textColor || 'text-gray-600'}`}>
                    {PENALTY_TYPES[penalty.type]?.label || penalty.type}
                  </span>
                </td>
                <td className="p-4 text-gray-600">{penalty.issueDate}</td>
                <td className="p-4 text-gray-600">{penalty.expiryDate}</td>
                <td className="p-4">
                  <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${
                    penalty.status === 'active' 
                      ? 'bg-red-100 text-red-700' 
                      : 'bg-gray-100 text-gray-600'
                  }`}>
                    {penalty.status === 'active' ? '활성' : '비활성'}
                  </span>
                </td>
                <td className="p-4 text-right">
                  <button 
                    onClick={() => handleManageClick(penalty)}
                    className="px-3 py-1.5 text-sm font-medium text-gray-600 hover:text-gray-900"
                  >
                    관리
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-xl shadow-lg w-[500px]">
            <div className="flex justify-between items-center p-6 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900">
                {selectedPenalty ? '패널티 관리' : '새 패널티 부여'}
              </h3>
              <button 
                onClick={() => {
                  setIsModalOpen(false);
                  resetForm();
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X size={20} />
              </button>
            </div>
            
            <div className="p-6 space-y-6">
              {!selectedPenalty && (
                <>
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">학번</label>
                    <input
                      type="text"
                      value={penaltyForm.studentId}
                      onChange={(e) => handleFormChange('studentId', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-200 rounded-lg text-sm"
                      placeholder="학번을 입력하세요"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">이름</label>
                    <input
                      type="text"
                      value={penaltyForm.name}
                      onChange={(e) => handleFormChange('name', e.target.value)}
                      className="w-full px-4 py-2 border border-gray-200 rounded-lg text-sm"
                      placeholder="이름을 입력하세요"
                    />
                  </div>
                </>
              )}

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">패널티 종류</label>
                <div className="grid grid-cols-3 gap-3">
                  {Object.entries(PENALTY_TYPES).map(([key, value]) => (
                    <button
                      key={key}
                      onClick={() => handleFormChange('type', key)}
                      className={`p-3 rounded-lg text-sm font-medium border transition-colors ${
                        penaltyForm.type === key
                          ? 'border-gray-900 bg-gray-900 text-white'
                          : 'border-gray-200 text-gray-600 hover:border-gray-400'
                      }`}
                    >
                      {value.label}
                      <span className="block text-xs mt-1 opacity-75">
                        {value.days}일
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">부여 날짜</label>
                <div className="relative">
                  <input
                    type="date"
                    value={penaltyForm.issueDate}
                    onChange={(e) => handleFormChange('issueDate', e.target.value)}
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg text-sm"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">만료일</label>
                <input
                  type="text"
                  value={penaltyForm.expiryDate}
                  disabled
                  className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm"
                />
              </div>
            </div>

            <div className="flex justify-between p-6 border-t border-gray-200 bg-gray-50">
              {selectedPenalty && (
                <button 
                  onClick={handleDeletePenalty}
                  className="px-4 py-2 text-sm font-medium text-red-600 hover:text-red-700 transition-colors"
                >
                  패널티 삭제
                </button>
              )}
              <div className="flex gap-3 ml-auto">
                <button 
                  onClick={() => {
                    setIsModalOpen(false);
                    resetForm();
                  }}
                  className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 transition-colors"
                >
                  취소
                </button>
                <button 
                  onClick={selectedPenalty ? handleUpdatePenalty : handleAddPenalty}
                  className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors"
                  disabled={!penaltyForm.type || !penaltyForm.issueDate || (!selectedPenalty && (!penaltyForm.studentId || !penaltyForm.name))}
                >
                  {selectedPenalty ? '수정하기' : '부여하기'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PenaltyManagement;