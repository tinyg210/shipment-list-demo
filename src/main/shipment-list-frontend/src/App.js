import React, {useCallback, useEffect, useState} from "react";
import {useDropzone} from 'react-dropzone';
import axios from "axios";
import './App.css';
import SSEManager from './SSEManager';
import placeholder from './placeholder.jpg';

const Shipments = () => {
  const [shipments, setShipments] = useState([]);
  const [isFetchingComplete, setIsFetchingComplete] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0); // hack0: Add refreshKey state to force refresh

  const fetchShipments = () => {
    axios.get("http://localhost:8081/api/shipment").then(res => {
      console.log(res);
      setShipments(res.data)
    }).then(() => {
      setIsFetchingComplete(true);
      setRefreshKey((prevKey) => prevKey + 1); // Update refreshKey

    })
    .catch((error) => {
      console.log(error);
    });
  }
  useEffect(() => {
    fetchShipments();
  }, [isFetchingComplete]);

  function handleRemove(shipmentId) {
    axios.delete(`http://localhost:8081/api/shipment/${shipmentId}`)
    .then(res => {
      console.log(res.data)
      const newList = shipments.filter(
          (shipment) => shipment.shipmentId !== shipmentId);
      setShipments(newList);
    }).catch(err => {
      console.log(err)
    });
  }

  const refreshShipmentPicture = (shipmentId) => {
    if(shipments.some((shp) => shp.shipmentId === shipmentId)) {
      setShipments(shipments);
      setRefreshKey((prevKey) => prevKey + 1); // Update refreshKey
    }
  }

  const handleSSEEvent = (data) => {
    if (isFetchingComplete) {
      refreshShipmentPicture(data);
      console.log("Message: " + data);
    }
  }

  const handleSSEError = (event) => {
    console.log("On error handler: " + event.target.readyState);
    if (event.target.readyState === EventSource.CLOSED) {
      console.log('eventsource closed (' + event.target.readyState + ')')
    }
  }

  return (
      <div key={refreshKey}>
        <SSEManager onEvent={handleSSEEvent} onError={handleSSEError}/>

        {shipments.map((shipment, index) => (
            <div key={index} style={{
              display: "flex",
              alignItems: "center", /* centers the items vertically */
              justifyContent: "center",
            }}>
              <div>
                <Dropzone {...shipment}/>
                <img src={`http://localhost:8081/api/shipment/${shipment.shipmentId}/image/download?t=${Date.now()}`} //hack1: cache busting to refresh
                     alt={placeholder}
                     style={{objectFit: "contain"}}/>

              </div>

              <div
                  style={{
                    display: "block",
                    textAlign: "left",
                    marginLeft: "20px"
                  }}>
                <br/>
                <br/>

                <h2> Shipment ID: {shipment.shipmentId}</h2>
                <h3>From: {shipment.sender.name}</h3>
                <h3>Address: {shipment.sender.address.postalCode} {shipment.sender.address.street} {shipment.sender.address.number} {shipment.sender.address.city}</h3>
                <h3>To: {shipment.recipient.name}</h3>
                <h3>Address: {shipment.recipient.address.postalCode} {shipment.recipient.address.street} {shipment.recipient.address.number} {shipment.recipient.address.city}</h3>
                <h3>Weight: {shipment.weight}</h3>
                <button className={"btn"}
                        onClick={() => handleRemove(shipment.shipmentId)}>
                  Delete forever
                </button>

                <br/>
              </div>
            </div>
        ))
        }
      </div>);
}

function Dropzone(
    {
      shipmentId
    }
) {
  const onDrop = useCallback(acceptedFiles => {
    const file = acceptedFiles[0];
    console.log(file);
    const formData = new FormData();
    formData.append("file", file);

    axios.post(
        `http://localhost:8081/api/shipment/${shipmentId}/image/upload`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data"
          }
        }).then(() => {
      console.log("File upload succeeded.")
    }).catch(err => {
      console.log(err)
    });
  }, [])
  const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop})

  return (
      <div {...getRootProps()}>
        <input {...getInputProps()} />
        {
          isDragActive ?
              (<p>Drop the image here ...</p>) :
              (
                  <div>
                    <h3 style={{margin: 0}}>Size (Banana for scale):</h3>
                    <h5 style={{margin: 0}}>click to add new image</h5>
                  </div>)

        }
      </div>
  )
}

function App() {
  return (
      <div className="App" style={{
        paddingBottom: "100px"
      }}>
        <h1>Shipments you can see and edit</h1>
        <Shipments/>
      </div>
  );
}

export default App;
