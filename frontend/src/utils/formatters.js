export const formatFingerprintPosition = (position) => {
  if (!position) return "Không xác định";

  return position
    .replace("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
};

export const formatDate = (date) => {
  if (!date) return "Chưa có";

  return new Date(date).toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

export const formatFingerprintCount = (active, total) => {
  return `${active} / ${total}`;
};

export const truncateString = (str, maxLength = 20) => {
  if (!str) return "";

  return str.length > maxLength ? `${str.substring(0, maxLength)}...` : str;
};
