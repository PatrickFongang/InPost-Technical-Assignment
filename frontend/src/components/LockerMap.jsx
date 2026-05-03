import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
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

const LockerMap = () => {
  const centerPosition = [52.0, 19.0];

  return (
    <MapContainer center={centerPosition} zoom={5} className="h-full w-full z-0">
      
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <Marker position={[52.2297, 21.0122]}>
        <Popup>
          <div className="font-bold">Witaj w Warszawie!</div>
          <div className="text-gray-500 text-sm">Tu kiedyś będzie paczkomat.</div>
        </Popup>
      </Marker>

    </MapContainer>
  );
};

export default LockerMap;