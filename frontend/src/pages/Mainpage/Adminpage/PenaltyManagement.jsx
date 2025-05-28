import React, { useState } from 'react';
import { X, Trash2, RotateCcw } from 'lucide-react';
import usePenaltyLogic from './PenaltyLogic';

const PenaltyManagement = () => {
  const {
    penalties,
    isModalOpen,
    setIsModalOpen,
    penaltyForm,
    setPenaltyForm,
    handleFormChange,
    handleAddPenalty,
    handleDeletePenalty,
    fetchPenalties, 
  } = usePenaltyLogic();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  const handleNewPenalty = () => {
    setPenaltyForm({
      studentId: '',
      name: '',
      type: '',
      issueDate: new Date().toISOString().split('T')[0],
      expiryDate: ''
    });
    setIsModalOpen(true);
  };

  const filteredPenalties = penalties.filter(penalty => {
    const matchesSearch =
      penalty.name.includes(searchQuery) || penalty.studentId.includes(searchQuery);
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && penalty.status === 'active') ||
      (statusFilter === 'inactive' && penalty.status !== 'active');

    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      {/* 🔹 검색창과 버튼 정렬 */}
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-900">페널티 관리</h2>
        <div className="flex gap-2">
          <button
            onClick={() => {
              fetchPenalties();
            }}
            className="px-4 py-2 flex items-center bg-gray-600 text-white rounded-lg text-sm font-medium hover:bg-gray-500 transition-colors"
          >
            <RotateCcw size={16} className="mr-1" />
            목록 새로고침
          </button>
          <button
            onClick={handleNewPenalty}
            className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors"
          >
            새 페널티 부여
          </button>
        </div>
      </div>

      <div className="flex items-center gap-4 mb-4">
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm"
          placeholder="학번 또는 이름 검색"
        />
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg text-sm w-32"
        >
          <option value="all">전체</option>
          <option value="active">활성</option>
          <option value="inactive">비활성</option>
        </select>
      </div>

      {/* 페널티 목록 테이블 */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 text-sm font-medium text-gray-600">
            <tr>
              <th className="text-left p-4">학생 정보</th>
              <th className="text-left p-4">페널티 종류</th>
              <th className="text-left p-4">부여 날짜</th>
              <th className="text-left p-4">만료일</th>
              <th className="text-left p-4">상태</th>
              <th className="text-left p-4">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {filteredPenalties.map((penalty, index) => (
              <tr key={index} className="text-sm">
                <td className="p-4">
                  <div className="font-medium text-gray-900">{penalty.name}</div>
                  <div className="text-gray-500">{penalty.studentId}</div>
                </td>
                <td className="p-4 text-gray-600">{penalty.type}</td>
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
                <td className="p-4">
                  {penalty.status !== 'inactive' && (
                  <button 
                  onClick={() => handleDeletePenalty(penalty.studentId)}
                  className="px-3 py-1 bg-red-500 text-white rounded-lg text-xs font-medium hover:bg-red-600 transition flex items-center"
                  >
                  <Trash2 size={14} className="mr-1" />
                  해지
                  </button>
                  )}
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
              <h3 className="text-lg font-semibold text-gray-900">새 페널티 부여</h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X size={20} />
              </button>
            </div>

            <div className="p-6 space-y-6">
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
                <label className="block text-sm font-medium text-gray-700">만료일</label>
                <input
                  type="date"
                  value={penaltyForm.expiryDate}
                  onChange={(e) => handleFormChange('expiryDate', e.target.value)}
                  className="w-full px-4 py-2 border border-gray-200 rounded-lg text-sm"
                />
              </div>
            </div>

            <div className="flex justify-between p-6 border-t border-gray-200 bg-gray-50">
              <button 
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 transition-colors"
              >
                취소
              </button>
              <button 
                onClick={handleAddPenalty}
                className="px-4 py-2 bg-gray-900 text-white rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors"
              >
                부여하기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PenaltyManagement;
