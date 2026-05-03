import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import { useEffect } from "react";
import "leaflet/dist/leaflet.css";

import L from "leaflet";
import icon from "leaflet/dist/images/marker-icon.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

const MapUpdater = ({ center }) => {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.flyTo(center, 13, { duration: 1.5 });
    }
  }, [center, map]);
  return null;
};

const LockerMap = ({ centerPosition }) => {
  return (
    <MapContainer center={[52.0, 19.0]} zoom={5} className="h-full w-full z-0">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <MapUpdater center={centerPosition} />

      <Marker position={[52.2297, 21.0122]}>
        <Popup>
          <div className="font-bold">Welcome to Warsaw!</div>
          <div className="text-gray-500 text-sm">
            Some lockers are available here. Click on the map to find more lockers near you.
          </div>
        </Popup>
      </Marker>
    </MapContainer>
  );
};

export default LockerMap;
