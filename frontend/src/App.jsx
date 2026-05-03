import { useState } from "react";
import LockerMap from "./components/LockerMap";

function App() {
  return (
    <div className="h-screen w-full flex flex-col bg-white overflow-hidden">
      <header className="h-20 bg-white border-b border-gray-200 flex items-center px-6 shadow-sm z-10">
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 bg-yellow-400 rounded-xl flex items-center justify-center font-bold text-xl text-black">
            📦
          </div>
          <h1 className="text-2xl font-extrabold text-gray-800 tracking-tight">
            Smart<span className="text-yellow-500">Picker</span>
          </h1>
        </div>

        <div className="ml-10 text-gray-400 text-sm italic">
          (Search bar and filters will go here)
        </div>
      </header>

      <main className="flex-1 flex overflow-hidden">
        <section className="w-1/3 min-w-[350px] max-w-[450px] bg-gray-50 border-r border-gray-200 overflow-y-auto p-4 flex flex-col gap-4">
          <h2 className="font-semibold text-gray-700 mb-2">Nearby Lockers</h2>

          <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 h-32 flex items-center justify-center text-gray-400">
            Search results will appear here...
          </div>
        </section>

        <section className="flex-1 relative z-0">
          <LockerMap />
        </section>
      </main>
    </div>
  );
}

export default App;
