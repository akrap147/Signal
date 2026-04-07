export default function Input({ className, ...props }) {
  return (
    <input
      className={`w-full px-3 py-2 bg-zinc-800 text-white border border-zinc-700 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-zinc-500 ${className}`}
      {...props}
    />
  );
}
