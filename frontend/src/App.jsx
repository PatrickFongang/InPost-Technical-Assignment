import { useState } from "react";
import LockerMap from "./components/LockerMap";
import { Search, MapPin } from "lucide-react";

function App() {
  const [address, setAddress] = useState("");
  const [radius, setRadius] = useState(5);
  const [easyAccess, setEasyAccess] = useState(false);
  const [antiBounce, setAntiBounce] = useState(false);
  const [thermo, setThermo] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault(); 
    console.log("Szukam z parametrami:", { address, radius, easyAccess, antiBounce, thermo });
  };

  return (
    <div className="h-screen w-full flex flex-col bg-white overflow-hidden">
      
      <header className="h-20 bg-white border-b border-gray-200 flex items-center px-6 shadow-sm z-10 justify-between">
        
        <div className="flex items-center gap-4 min-w-max">
          <div className="w-10 h-10 bg-yellow-400 rounded-xl flex items-center justify-center font-bold text-xl text-black">
            📦
          </div>
          <h1 className="text-2xl font-extrabold text-gray-800 tracking-tight">
            Smart<span className="text-yellow-500">Picker</span>
          </h1>
        </div>

        <form onSubmit={handleSearch} className="flex-1 max-w-4xl ml-8 flex items-center gap-4">
          
          <div className="relative flex-1">
            <MapPin className="absolute left-3 top-2.5 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Enter city or address..."
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 outline-none transition-all"
              required
            />
          </div>

          <select 
            value={radius} 
            onChange={(e) => setRadius(Number(e.target.value))}
            className="py-2 px-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 outline-none"
          >
            <option value={1}>1 km</option>
            <option value={2}>2 km</option>
            <option value={5}>5 km</option>
            <option value={10}>10 km</option>
          </select>

          <div className="flex items-center gap-4 px-2 border-l border-gray-200">
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
              <input 
                type="checkbox" 
                checked={easyAccess} 
                onChange={(e) => setEasyAccess(e.target.checked)}
                className="w-4 h-4 text-yellow-500 rounded focus:ring-yellow-400 cursor-pointer" 
              />
              Easy Access
            </label>

            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
              <input 
                type="checkbox" 
                checked={antiBounce} 
                onChange={(e) => setAntiBounce(e.target.checked)}
                className="w-4 h-4 text-yellow-500 rounded focus:ring-yellow-400 cursor-pointer" 
              />
              Anti-Bounce
            </label>

            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
              <input 
                type="checkbox" 
                checked={thermo} 
                onChange={(e) => setThermo(e.target.checked)}
                className="w-4 h-4 text-blue-500 rounded focus:ring-blue-400 cursor-pointer" 
              />
              Thermo Mode
            </label>
          </div>

          <button 
            type="submit" 
            className="bg-yellow-400 hover:bg-yellow-500 text-black font-bold py-2 px-6 rounded-lg transition-colors flex items-center gap-2 shadow-sm"
          >
            <Search className="w-5 h-5" />
            Search
          </button>
        </form>

      </header>

      <main className="flex-1 flex overflow-hidden">
        
        <section className="w-1/3 min-w-[350px] max-w-[450px] bg-gray-50 border-r border-gray-200 overflow-y-auto p-4 flex flex-col gap-4">
          <h2 className="font-semibold text-gray-700 mb-2">Nearby Lockers</h2>
          
          <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 h-32 flex items-center justify-center text-gray-400 text-center px-8">
            Enter an address and hit Search to find the best lockers nearby.
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