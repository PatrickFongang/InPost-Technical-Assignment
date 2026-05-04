import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { useEffect } from 'react';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

const MapUpdater = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.flyTo(center, 14, { duration: 1.5 });
    }
  }, [center, map]);
  return null;
};

const LockerMap = ({ centerPosition, lockers = [] }) => {
  return (
    <MapContainer center={[52.0, 19.0]} zoom={5} className="h-full w-full z-0">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      
      <MapUpdater center={centerPosition} />

      {lockers.map((locker) => {
        if (!locker.location || !locker.location.latitude || !locker.location.longitude) return null;

        return (
          <Marker 
            key={locker.name} 
            position={[locker.location.latitude, locker.location.longitude]}
          >
            <Popup>
              <div className="font-bold text-gray-800">{locker.name}</div>
              <div className="text-gray-500 text-sm mb-3">
                {locker.address_details?.city}, {locker.address_details?.street}
              </div>
              <div className="flex flex-col gap-1">
                
                <div className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 inline-block rounded border ${
                  locker.easyAccessReliability?.toUpperCase() === 'HIGH' ? 'bg-green-100 text-green-700 border-green-200' :
                  locker.easyAccessReliability?.toUpperCase() === 'MEDIUM' ? 'bg-orange-100 text-orange-700 border-orange-200' :
                  locker.easyAccessReliability?.toUpperCase() === 'LOW' ? 'bg-red-100 text-red-700 border-red-200' :
                  'bg-gray-100 text-gray-500 border-gray-200'
                }`}>
                  Easy Access: {locker.easyAccessReliability || 'N/A'}
                </div>

                {/* Poprawiony klucz dla dymku z mapy */}
                <div className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 inline-block rounded border ${
                  locker.stressFreeReliability?.toUpperCase() === 'HIGH' ? 'bg-green-100 text-green-700 border-green-200' :
                  locker.stressFreeReliability?.toUpperCase() === 'MEDIUM' ? 'bg-orange-100 text-orange-700 border-orange-200' :
                  locker.stressFreeReliability?.toUpperCase() === 'LOW' ? 'bg-red-100 text-red-700 border-red-200' :
                  'bg-gray-100 text-gray-500 border-gray-200'
                }`}>
                  Stress Free: {locker.stressFreeReliability || 'N/A'}
                </div>
                
              </div>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
};

export default LockerMap;