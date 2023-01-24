import React, {useCallback, useEffect, useState} from "react";
import {useDropzone} from 'react-dropzone'
import axios from "axios";
import './App.css';
import placeholder from './placeholder.jpg';

const ShipmentImage = ({shipmentId}) => {
  const [image, setImage] = useState(null);

  const fetchImage = () => {
    axios.get(
        `http://localhost:8081/api/shipment/${shipmentId}/image/download`,
        {responseType: 'blob'})
    .then(res => {
          setImage(URL.createObjectURL(res.data));
        }
    )
    .catch(error => {
      if (error.response || error.request) {
        setImage(null);
      }
    });

  }
  useEffect(() => {
    fetchImage();
  }, []);

  return (
      <img src={image ? image : placeholder}/>
  );
}

const Shipments = () => {
  const [shipments, setShipments] = useState([]);

  const fetchShipments = () => {
    axios.get("http://localhost:8081/api/shipment").then(res => {
      console.log(res);
      setShipments(res.data)
    })
  }
  useEffect(() => {
    fetchShipments();
  }, []);

  return shipments.map((shipment, index) => {
    return (
        <div key={index} style={{
          display: "inline-flex",
          float: "left",
          marginLeft: "20px"
        }}>
          <div>
            <Dropzone {...shipment}/>

            <ShipmentImage shipmentId={shipment.shipmentId}/>

          </div>

          <div
              style={{display: "block", textAlign: "left", marginLeft: "20px"}}>
            <br/>
            <br/>

            <h2> Shipment ID: {shipment.shipmentId}</h2>
            <h3>From: {shipment.sender.name}</h3>
            <h3>Address: {shipment.sender.address.postalCode} {shipment.sender.address.street} {shipment.sender.address.number} {shipment.sender.address.city}</h3>
            <h3>To: {shipment.recipient.name}</h3>
            <h3>Address: {shipment.recipient.address.postalCode} {shipment.recipient.address.street} {shipment.recipient.address.number} {shipment.recipient.address.city}</h3>
            <h3>Weight: {shipment.weight}</h3>

                {/*<h3>Weigth: <form>*/}
                {/*  <input type="text" value={shipment.weight} onChange={})}/>*/}
                {/*</form></h3>*/}
                <br/>
          </div>
        </div>
  );
  });
  }



  function Dropzone(
    {
      shipmentId
    }
  )
    {
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

  function App()
    {
      return (
          <div className="App">
            <h1>Shipments you are allowed to see based on your account</h1>
            <Shipments/>
          </div>
      );
    }

  export default App;
