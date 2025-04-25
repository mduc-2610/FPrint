// Định dạng vị trí dấu vân tay
export const formatFingerprintPosition = (position) => {
  if (!position) return "Không xác định";

  return position
    .replace("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
};

// Định dạng ngày tháng
export const formatDate = (date) => {
  if (!date) return "Chưa có";

  return new Date(date).toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

// Định dạng số lượng dấu vân tay
export const formatFingerprintCount = (active, total) => {
  return `${active} / ${total}`;
};

// Hàm tiện ích để cắt ngắn chuỗi
export const truncateString = (str, maxLength = 20) => {
  if (!str) return "";

  return str.length > maxLength ? `${str.substring(0, maxLength)}...` : str;
};
