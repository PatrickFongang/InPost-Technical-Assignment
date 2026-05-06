import { useState } from "react";
import LockerMap from "./components/LockerMap";
import {
  Search,
  MapPin,
  Loader2,
  AlertTriangle,
  ThermometerSnowflake,
  Calendar,
  Info,
  X,
  LocateFixed,
} from "lucide-react";

function App() {
  const [address, setAddress] = useState("");
  const [radius, setRadius] = useState(5);
  const [thermo, setThermo] = useState(false);
  const [deliveryDate, setDeliveryDate] = useState("");

  const [lockers, setLockers] = useState([]);
  const [weatherInfo, setweatherInfo] = useState(null);

  const [mapCenter, setMapCenter] = useState(null);
  const [selectedLocker, setSelectedLocker] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);

  const today = new Date();
  const minDate = today.toISOString().split("T")[0];
  const maxDateObj = new Date();
  maxDateObj.setDate(today.getDate() + 14);
  const maxDate = maxDateObj.toISOString().split("T")[0];

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!address.trim()) return;

    setIsLoading(true);
    setError(null);
    setweatherInfo(null);
    setSelectedLocker(null);

    try {
      let userLat, userLon;

      if (address === "My Location" && mapCenter) {
        userLat = mapCenter[0];
        userLon = mapCenter[1];
      } else {
        const formattedAddress = address
          .trim()
          .split(" ")
          .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
          .join(" ");

        const geoUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(formattedAddress)}`;
        const geoResponse = await fetch(geoUrl);
        const geoData = await geoResponse.json();

        if (!geoData || geoData.length === 0) {
          setError("Location not found. Try a different city or address.");
          setIsLoading(false);
          return;
        }

        userLat = parseFloat(geoData[0].lat);
        userLon = parseFloat(geoData[0].lon);
        setMapCenter([userLat, userLon]);
      }

      const apiUrl = `http://localhost:8080/api/lockers/search`;
      const requestBody = {
        userLat: userLat,
        userLon: userLon,
        radiusInKm: radius,
        thermoMode: thermo,
        expectedDeliveryDate: deliveryDate ? deliveryDate : null,
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
    } catch (err) {
      console.error(err);
      setError("Network error while searching for location.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleLocateMe = () => {
    if ("geolocation" in navigator) {
      setIsLoading(true);
      setError(null);
      setweatherInfo(null);
      setSelectedLocker(null);

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            const userLat = position.coords.latitude;
            const userLon = position.coords.longitude;
            setMapCenter([userLat, userLon]);
            setAddress("My Location"); 

            const apiUrl = `http://localhost:8080/api/lockers/search`;
            const requestBody = {
              userLat: userLat,
              userLon: userLon,
              radiusInKm: radius,
              thermoMode: thermo,
              expectedDeliveryDate: deliveryDate ? deliveryDate : null,
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
          } catch (err) {
            console.error(err);
            setError("Network error while searching for location.");
          } finally {
            setIsLoading(false);
          }
        },
        (error) => {
          setError("Could not get your location. Please check browser permissions.");
          setIsLoading(false);
        }
      );
    } else {
      setError("Geolocation is not supported by your browser.");
    }
  };

  const shouldShowWeatherWarning =
    weatherInfo && !thermo && weatherInfo.isExtreme;

  return (
    <div className="h-screen w-full flex flex-col bg-white overflow-hidden">
      {isInfoModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-2xl w-full p-6 relative overflow-y-auto max-h-full">
            <button
              onClick={() => setIsInfoModalOpen(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
            >
              <X className="w-6 h-6" />
            </button>
            <h2 className="text-2xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <Info className="text-blue-500 w-6 h-6" />
              How SmartPicker Works
            </h2>
            <div className="space-y-4 text-gray-600 text-sm leading-relaxed">
              <p>
                <strong>SmartPicker</strong> helps you find the most convenient
                lockers based on your location, weather, and specific machine
                attributes.
              </p>

              <div className="grid md:grid-cols-2 gap-4">
                <div className="bg-blue-50 p-3 rounded-lg border border-blue-100">
                  <h3 className="font-semibold text-blue-800 mb-1 flex items-center gap-1">
                    <ThermometerSnowflake className="w-4 h-4" /> Thermo Mode
                  </h3>
                  <p className="text-blue-700 text-xs">
                    Filters lockers to show only those in temperature-controlled
                    environments. Crucial for sensitive packages during extreme
                    weather (e.g., freezing or heatwaves).
                  </p>
                </div>

                <div className="bg-blue-50 p-3 rounded-lg border border-blue-100">
                  <h3 className="font-semibold text-blue-800 mb-1 flex items-center gap-1">
                    <Calendar className="w-4 h-4" /> Expected Delivery Date
                  </h3>
                  <p className="text-blue-700 text-xs">
                    Allows you to check the weather forecast for your delivery
                    day (up to 14 days ahead). If extreme weather is predicted,
                    we will alert you to enable Thermo Mode.
                  </p>
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4 mt-4">
                <div className="border border-gray-200 p-3 rounded-lg bg-gray-50">
                  <h3 className="font-semibold text-gray-800 mb-2 border-b pb-1">
                    Easy Access Score
                  </h3>
                  <ul className="text-xs space-y-1 list-disc pl-4">
                    <li>
                      <strong className="text-green-600">HIGH:</strong> Staffed
                      points (POK/POP) ensuring direct human assistance.
                    </li>
                    <li>
                      <strong className="text-orange-500">MEDIUM:</strong>{" "}
                      Machine has an Easy Access Zone and low traffic.
                    </li>
                    <li>
                      <strong className="text-red-500">LOW:</strong> Machine has
                      an Easy Access Zone, but usually experiences high traffic.
                    </li>
                    <li>
                      <strong className="text-gray-500">NONE:</strong> Machine
                      lacks an Easy Access Zone.
                    </li>
                  </ul>
                  <p className="mt-2 text-[10px] text-gray-500 italic leading-tight">
                    *Note: You must enable the "Easy Access" feature in the
                    official InPost app. This score only estimates the actual
                    probability of securing a lower locker.
                  </p>
                </div>

                <div className="border border-gray-200 p-3 rounded-lg bg-gray-50">
                  <h3 className="font-semibold text-gray-800 mb-2 border-b pb-1">
                    Stress Free Score
                  </h3>
                  <ul className="text-xs space-y-1 list-disc pl-4">
                    <li>
                      <strong className="text-green-600">HIGH:</strong> Premium
                      or staffed points (SuperPOP, POK, POP).
                    </li>
                    <li>
                      <strong className="text-orange-500">MEDIUM:</strong>{" "}
                      Standard machine but with low current interest/traffic.
                    </li>
                    <li>
                      <strong className="text-red-500">LOW:</strong> Standard
                      machine that usually experiences high traffic (potential
                      queues).
                    </li>
                  </ul>
                </div>
              </div>
            </div>
            <button
              onClick={() => setIsInfoModalOpen(false)}
              className="mt-6 w-full bg-gray-100 hover:bg-gray-200 text-gray-800 font-semibold py-2 rounded-lg transition-colors"
            >
              Got it!
            </button>
          </div>
        </div>
      )}

      <header className="min-h-20 bg-white border-b border-gray-200 flex items-center px-4 md:px-6 shadow-sm z-10 overflow-x-auto shrink-0 py-3 md:py-0">
        <div className="flex items-center gap-3 min-w-max mr-6">
          <div className="w-10 h-10 bg-yellow-400 rounded-xl flex items-center justify-center font-bold text-xl text-black">
            📦
          </div>
          <h1 className="text-2xl font-extrabold text-gray-800 tracking-tight flex items-center gap-2">
            <span>
              Smart<span className="text-yellow-500">Picker</span>
            </span>
            <button
              onClick={() => setIsInfoModalOpen(true)}
              className="text-gray-400 hover:text-blue-500 transition-colors p-1 rounded-full hover:bg-blue-50"
              title="How it works"
            >
              <Info className="w-5 h-5" />
            </button>
          </h1>
        </div>

        <form
          onSubmit={handleSearch}
          className="flex flex-1 items-center gap-3 w-full min-w-[600px]"
        >
          <div className="relative flex-1 min-w-[200px]">
            <MapPin className="absolute left-3 top-2.5 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="City or address..."
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 outline-none transition-all"
              required
            />
            <button
              type="button"
              onClick={handleLocateMe}
              className="absolute right-2 top-2 text-blue-500 hover:text-blue-700 transition-colors p-0.5 rounded-md hover:bg-blue-50"
              title="Use my current location"
            >
              <LocateFixed className="w-5 h-5" />
            </button>
          </div>

          <select
            value={radius}
            onChange={(e) => setRadius(Number(e.target.value))}
            className="py-2 px-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 outline-none cursor-pointer shrink-0"
          >
            <option value={1}>1 km</option>
            <option value={2}>2 km</option>
            <option value={5}>5 km</option>
            <option value={10}>10 km</option>
          </select>

          <div className="relative shrink-0 w-44">
            <Calendar className="absolute left-3 top-2.5 text-gray-400 w-5 h-5" />
            <input
              type="date"
              value={deliveryDate}
              min={minDate}
              max={maxDate}
              onChange={(e) => setDeliveryDate(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 outline-none transition-all cursor-pointer text-gray-700"
              title="Expected Delivery Date (optional)"
            />
          </div>

          <div className="flex items-center shrink-0">
            <label className="flex items-center gap-2 text-sm font-semibold text-blue-600 cursor-pointer bg-blue-50 px-4 py-2 rounded-lg border border-blue-100 hover:bg-blue-100 transition-colors w-full justify-center">
              <input
                type="checkbox"
                checked={thermo}
                onChange={(e) => setThermo(e.target.checked)}
                className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 cursor-pointer"
              />
              <ThermometerSnowflake className="w-4 h-4" />
              Thermo
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="bg-yellow-400 hover:bg-yellow-500 disabled:bg-yellow-200 text-black font-bold py-2 px-6 rounded-lg transition-colors flex items-center justify-center gap-2 shadow-sm shrink-0 min-w-[120px]"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Search className="w-5 h-5" />
            )}
            Search
          </button>
        </form>
      </header>

      {shouldShowWeatherWarning && (
        <div className="bg-yellow-50 border-b border-yellow-200 px-6 py-3 flex items-center gap-3 shrink-0">
          <AlertTriangle className="text-yellow-500 w-5 h-5 shrink-0" />
          <p className="text-sm text-yellow-800">
            <strong>Weather Alert:</strong> Extreme temperatures detected for
            your delivery date (from {weatherInfo.minTemp}°C to{" "}
            {weatherInfo.maxTemp}°C). We highly recommend enabling{" "}
            <strong>Thermo Mode</strong>.
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

          {lockers.map((locker) => {
            const isSelected = selectedLocker === locker.name;

            return (
              <div
                key={locker.name}
                onClick={() => {
                  if (locker.location?.latitude && locker.location?.longitude) {
                    setMapCenter([
                      locker.location.latitude,
                      locker.location.longitude,
                    ]);
                    setSelectedLocker(locker.name);
                  }
                }}
                className={`shrink-0 bg-white p-4 rounded-xl shadow-sm border flex gap-4 hover:shadow-md transition-all cursor-pointer relative overflow-hidden
                  ${isSelected ? "border-yellow-400 ring-2 ring-yellow-100" : "border-gray-100"}
                `}
              >
                <div
                  className={`absolute left-0 top-0 bottom-0 w-1 ${
                    locker.easyAccessReliability?.toUpperCase() === "HIGH"
                      ? "bg-green-500"
                      : locker.easyAccessReliability?.toUpperCase() === "MEDIUM"
                        ? "bg-orange-400"
                        : locker.easyAccessReliability?.toUpperCase() === "LOW"
                          ? "bg-red-500"
                          : "bg-gray-300"
                  }`}
                />

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
                          : locker.easyAccessReliability?.toUpperCase() ===
                              "MEDIUM"
                            ? "bg-orange-100 text-orange-700 border-orange-200"
                            : locker.easyAccessReliability?.toUpperCase() ===
                                "LOW"
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
                          : locker.stressFreeReliability?.toUpperCase() ===
                              "MEDIUM"
                            ? "bg-orange-100 text-orange-700 border-orange-200"
                            : locker.stressFreeReliability?.toUpperCase() ===
                                "LOW"
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
            );
          })}
        </section>

        <section className="flex-1 relative z-0">
          <LockerMap
            centerPosition={mapCenter}
            lockers={lockers}
            selectedLocker={selectedLocker}
          />
        </section>
      </main>
    </div>
  );
}

export default App;
