import { useState, useEffect, useCallback } from "react";
import { useTokenHandler } from "../handlers/TokenHandler";

const useQRCodeFetcher = (resId) => {
  const [qrToken, setQrCode] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [qrStatus, setQrStatus] = useState(null);
  const { refreshTokens } = useTokenHandler();

  const fetchQRCode = useCallback(async (retry = true) => {
    if (!resId || qrToken) return;

    setLoading(true);
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) {
        setLoading(false);
        return;
      }
      const response = await fetch(`/api/reservations/my/${resId}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
      });

      setQrStatus(response.status);

      if (response.status === 401 && retry) {
        accessToken = await refreshTokens();

        if (accessToken) {
          return fetchQRCode(false);
        } else {
          setLoading(false);
          return;
        }
      }

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const json = await response.json();
      setQrCode(json.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [resId, qrToken])

  const sendQRCodeToServer = useCallback(async () => {
    if (!qrToken) {
      return;
    }

    setLoading(true);
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) {
        setLoading(false);
        return;
      }

      const response = await fetch(`/api/qr/recognize`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ qrToken }),
      });

      if (response.status === 401) {
        accessToken = await refreshTokens();

        if (accessToken) {
          return sendQRCodeToServer();
        } else {
          setLoading(false);
          return;
        }
      }

      if (response.status === 418) {
        setLoading(false);
        return;
      }

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      await response.json();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [qrToken, refreshTokens]);

  useEffect(() => {
    if (resId && !qrToken) {
      fetchQRCode();
    }
  }, [resId, qrToken]);

  return { qrToken, qrStatus, error, loading, sendQRCodeToServer };
};

export default useQRCodeFetcher;