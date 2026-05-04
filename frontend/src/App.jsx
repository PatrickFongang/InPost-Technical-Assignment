import { useState } from "react";
import LockerMap from "./components/LockerMap";
import {
  Search,
  MapPin,
  Loader2,
  AlertTriangle,
  ThermometerSnowflake,
  Calendar
} from "lucide-react";

function App() {
  const [address, setAddress] = useState("");
  const [radius, setRadius] = useState(5);
  const [thermo, setThermo] = useState(false);
  const [deliveryDate, setDeliveryDate] = useState("");

  const [lockers, setLockers] = useState([]);
  const [weatherInfo, setweatherInfo] = useState(null);

  const [mapCenter, setMapCenter] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!address.trim()) return;

    setIsLoading(true);
    setError(null);
    setweatherInfo(null);

    try {
      const geoUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`;
      const geoResponse = await fetch(geoUrl);
      const geoData = await geoResponse.json();

      if (!geoData || geoData.length === 0) {
        setError("Location not found. Try a different city or address.");
        setIsLoading(false);
        return;
      }

      const userLat = parseFloat(geoData[0].lat);
      const userLon = parseFloat(geoData[0].lon);
      setMapCenter([userLat, userLon]);

      const apiUrl = `http://localhost:8080/api/lockers/search`;
      const requestBody = {
        userLat: userLat,
        userLon: userLon,
        radiusInKm: radius,
        thermoMode: thermo,
        expectedDeliveryDate: deliveryDate ? deliveryDate : null
      };

      const backendResponse = await fetch(apiUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      if (!backendResponse.ok)
        throw new Error(`Error from backend: ${backendResponse.status}`);

      const responseData = await backendResponse.json();

      setLockers(responseData.lockers || []);
      setweatherInfo(responseData.weatherInfo || null);
      console.log("Weather Info received:", responseData.weatherInfo);
    } catch (err) {
      console.error(err);
      setError("Network error while searching for location.");
    } finally {
      setIsLoading(false);
    }
  };

  const shouldShowWeatherWarning =
    weatherInfo && !thermo && weatherInfo.isExtreme;

  return (
    <div className="h-screen w-full flex flex-col bg-white overflow-hidden">
      <header className="h-20 bg-white border-b border-gray-200 flex items-center px-6 shadow-sm z-10 justify-between shrink-0">
        <div className="flex items-center gap-4 min-w-max">
          <div className="w-10 h-10 bg-yellow-400 rounded-xl flex items-center justify-center font-bold text-xl text-black">
            📦
          </div>
          <h1 className="text-2xl font-extrabold text-gray-800 tracking-tight">
            Smart<span className="text-yellow-500">Picker</span>
          </h1>
        </div>

        <form
          onSubmit={handleSearch}
          className="flex-1 max-w-5xl ml-8 flex items-center gap-3"
        >
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
            className="py-2 px-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 outline-none shrink-0 cursor-pointer"
          >
            <option value={1}>1 km</option>
            <option value={2}>2 km</option>
            <option value={5}>5 km</option>
            <option value={10}>10 km</option>
          </select>

          <div className="relative shrink-0">
            <Calendar className="absolute left-3 top-2.5 text-gray-400 w-5 h-5" />
            <input
              type="date"
              value={deliveryDate}
              onChange={(e) => setDeliveryDate(e.target.value)}
              className="pl-10 pr-4 py-2 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 outline-none transition-all cursor-pointer text-gray-700"
              title="Expected Delivery Date (optional)"
            />
          </div>

          <div className="flex items-center gap-4 px-3 border-l border-gray-200 shrink-0">
            <label className="flex items-center gap-2 text-sm font-semibold text-blue-600 cursor-pointer bg-blue-50 px-3 py-2 rounded-lg border border-blue-100 hover:bg-blue-100 transition-colors">
              <input
                type="checkbox"
                checked={thermo}
                onChange={(e) => setThermo(e.target.checked)}
                className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 cursor-pointer"
              />
              <ThermometerSnowflake className="w-4 h-4" />
              Thermo Mode
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="bg-yellow-400 hover:bg-yellow-500 disabled:bg-yellow-200 text-black font-bold py-2 px-6 rounded-lg transition-colors flex items-center gap-2 shadow-sm shrink-0"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Search className="w-5 h-5" />
            )}
            {isLoading ? "Searching..." : "Search"}
          </button>
        </form>
      </header>

      {shouldShowWeatherWarning && (
        <div className="bg-yellow-50 border-b border-yellow-200 px-6 py-3 flex items-center gap-3 shrink-0">
          <AlertTriangle className="text-yellow-500 w-5 h-5 shrink-0" />
          <p className="text-sm text-yellow-800">
            <strong>Weather Alert:</strong> Extreme temperatures detected for your delivery date (from {weatherInfo.minTemp}°C to {weatherInfo.maxTemp}°C).
            We highly recommend enabling <strong>Thermo Mode</strong>.
          </p>
        </div>
      )}

      <main className="flex-1 flex overflow-hidden">
        <section className="w-1/3 min-w-[350px] max-w-[450px] bg-gray-50 border-r border-gray-200 overflow-y-auto p-4 flex flex-col gap-4">
          <h2 className="font-semibold text-gray-700 mb-2">
            Nearby Lockers{" "}
            {lockers.length > 0 && (
              <span className="text-sm font-normal text-gray-500">
                ({lockers.length} found)
              </span>
            )}
          </h2>

          {error && (
            <div className="text-red-500 text-sm bg-red-50 p-3 rounded-lg">
              {error}
            </div>
          )}

          {lockers.length === 0 && !isLoading && !error && (
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 h-32 flex items-center justify-center text-gray-400 text-center px-8">
              Enter an address and hit Search to find the best lockers nearby.
            </div>
          )}

          {lockers.map((locker) => (
            <div
              key={locker.name}
              className="shrink-0 bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex gap-4 hover:shadow-md transition-shadow cursor-pointer relative overflow-hidden"
            >
              <div className={`absolute left-0 top-0 bottom-0 w-1 ${
                locker.easyAccessReliability?.toUpperCase() === 'HIGH' ? 'bg-green-500' :
                locker.easyAccessReliability?.toUpperCase() === 'MEDIUM' ? 'bg-orange-400' :
                locker.easyAccessReliability?.toUpperCase() === 'LOW' ? 'bg-red-500' : 'bg-gray-300'
              }`} />

              <div className="w-14 h-14 shrink-0 bg-[#f8efe6] rounded-xl flex items-center justify-center text-2xl border border-orange-100 z-10 shadow-inner ml-1">
                📦
              </div>

              <div className="flex-1 flex flex-col justify-between z-10 min-w-0">
                <div>
                  <h3 className="font-bold text-gray-800 text-base truncate">
                    {locker.name}
                  </h3>
                  <p className="text-xs text-gray-500 leading-tight mt-0.5 truncate">
                    {locker.address_details?.city},{" "}
                    {locker.address_details?.street}{" "}
                    {locker.address_details?.building_number}
                  </p>
                </div>

                <div className="mt-3 flex flex-wrap items-center gap-2">
                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider border px-2 py-1 rounded shrink-0 ${
                      locker.easyAccessReliability?.toUpperCase() === "HIGH"
                        ? "bg-green-100 text-green-700 border-green-200"
                        : locker.easyAccessReliability?.toUpperCase() === "MEDIUM"
                        ? "bg-orange-100 text-orange-700 border-orange-200"
                        : locker.easyAccessReliability?.toUpperCase() === "LOW"
                        ? "bg-red-100 text-red-700 border-red-200"
                        : "bg-gray-100 text-gray-500 border-gray-200"
                    }`}
                  >
                    Easy Access: {locker.easyAccessReliability || "N/A"}
                  </span>

                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider border px-2 py-1 rounded shrink-0 ${
                      locker.stressFreeReliability?.toUpperCase() === "HIGH"
                        ? "bg-green-100 text-green-700 border-green-200"
                        : locker.stressFreeReliability?.toUpperCase() === "MEDIUM"
                        ? "bg-orange-100 text-orange-700 border-orange-200"
                        : locker.stressFreeReliability?.toUpperCase() === "LOW"
                        ? "bg-red-100 text-red-700 border-red-200"
                        : "bg-gray-100 text-gray-500 border-gray-200"
                    }`}
                  >
                    Stress Free: {locker.stressFreeReliability || "N/A"}
                  </span>

                  {locker.distance != null && (
                    <span className="text-[10px] font-bold uppercase tracking-wider bg-blue-50 text-blue-600 border border-blue-100 px-2 py-1 rounded shrink-0 ml-auto">
                      {locker.distance.toFixed(2)} km
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </section>

        <section className="flex-1 relative z-0">
          <LockerMap centerPosition={mapCenter} lockers={lockers} />
        </section>
      </main>
    </div>
  );
}

export default App;