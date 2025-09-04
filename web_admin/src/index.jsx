import React, {useEffect, useState} from "react";
import { initializeApp } from "firebase/app";
import { getFirestore, collection, onSnapshot, query } from "firebase/firestore";

const firebaseConfig = {
  // paste your firebase config here for local dev
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

function App() {
  const [orders, setOrders] = useState([]);
  const [users, setUsers] = useState([]);

  useEffect(() => {
    const q = query(collection(db, "orders"));
    return onSnapshot(q, (snap) => {
      setOrders(snap.docs.map(d => ({ id: d.id, ...(d.data()) })));
    });
  }, []);

  useEffect(() => {
    const q = query(collection(db, "users"));
    return onSnapshot(q, (snap) => {
      setUsers(snap.docs.map(d => ({ id: d.id, ...(d.data()) })));
    });
  }, []);

  return (
    <div style={{ padding: 20 }}>
      <h1>ServiceApp Admin</h1>
      <h2>Orders</h2>
      <table border="1" cellPadding="6">
        <thead><tr><th>ID</th><th>Service</th><th>Customer</th><th>Status</th></tr></thead>
        <tbody>
          {orders.map(o => <tr key={o.id}><td>{o.id}</td><td>{o.serviceType}</td><td>{o.customerId}</td><td>{o.status}</td></tr>)}
        </tbody>
      </table>
      <h2>Users</h2>
      <table border="1" cellPadding="6">
        <thead><tr><th>ID</th><th>Email</th><th>Role</th><th>Available</th></tr></thead>
        <tbody>
          {users.map(u => <tr key={u.id}><td>{u.id}</td><td>{u.email}</td><td>{u.role}</td><td>{String(u.available)}</td></tr>)}
        </tbody>
      </table>
    </div>
  );
}

export default App;

import { createRoot } from "react-dom/client";
createRoot(document.getElementById("root")).render(<App />);