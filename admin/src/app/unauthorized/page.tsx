export default function UnauthorizedPage() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <h1 className="text-4xl font-bold mb-4 text-red-600">Unauthorized</h1>
      <p className="text-xl">You do not have permission to access the admin dashboard.</p>
    </div>
  )
}
