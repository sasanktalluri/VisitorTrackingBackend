<div className="container py-4" style={{ backgroundImage: 'url("https://www.transparenttextures.com/patterns/white-wall-3.png")', backgroundColor: '#fff' }}>
  <h1 className="mb-4 text-center">VisitorIngestion</h1>

  <ul className="nav nav-tabs mb-3">
    <li className="nav-item">
      <button className={`nav-link ${activeSection === 'register' ? 'active' : ''}`} onClick={() => setActiveSection('register')}>Register</button>
    </li>
    <li className="nav-item">
      <button className={`nav-link ${activeSection === 'checkin' ? 'active' : ''}`} onClick={() => setActiveSection('checkin')}>Check-In/Out</button>
    </li>
    <li className="nav-item">
      <button className={`nav-link ${activeSection === 'service' ? 'active' : ''}`} onClick={() => setActiveSection('service')}>Service</button>
    </li>
    <li className="nav-item">
      <button className={`nav-link ${activeSection === 'payment' ? 'active' : ''}`} onClick={() => setActiveSection('payment')}>Payment</button>
    </li>
  </ul>

  {activeSection === 'register' && (
    <div className="card mb-3">
      <div className="card-header">Register Visitor</div>
      <div className="card-body d-flex gap-3 flex-wrap">
        <input className="form-control w-auto" placeholder="Visitor Name" value={newVisitorName} onChange={(e) => setNewVisitorName(e.target.value)} />
        <input className="form-control w-auto" placeholder="Phone Number" value={newVisitorPhone} onChange={(e) => setNewVisitorPhone(e.target.value)} />
        <input className="form-control w-auto" placeholder="Address" value={newVisitorAddress} onChange={(e) => setNewVisitorAddress(e.target.value)} />
        <button className="btn btn-dark" onClick={handleRegisterVisitor}>Register</button>
      </div>
    </div>
  )}

  {activeSection === 'checkin' && (
    <div className="card mb-3">
      <div className="card-header">Visitor Check-In / Check-Out</div>
      <div className="card-body row g-2">
        <div className="col-md-4">
          <input className="form-control" placeholder="Phone Number" value={checkInPhone} onChange={(e) => setCheckInPhone(e.target.value)} />
        </div>
        <div className="col-md-4">
          <button className="btn btn-success w-100" onClick={handleCheckIn}>Check In</button>
        </div>
        <div className="col-md-4">
          <button className="btn btn-danger w-100" onClick={handleCheckOut}>Check Out</button>
        </div>
      </div>
    </div>
  )}

  {activeSection === 'service' && (
    <div className="card mb-3">
      <div className="card-header">Add Service</div>
      <div className="card-body d-flex gap-3 flex-wrap">
        <input className="form-control w-auto" placeholder="Phone Number" value={servicePhone} onChange={(e) => setServicePhone(e.target.value)} />
        <input className="form-control w-auto" placeholder="Name" value={serviceName} onChange={(e) => setServiceName(e.target.value)} />
        <input className="form-control w-auto" placeholder="Service Type" value={serviceType} onChange={(e) => setServiceType(e.target.value)} />
        <button className="btn btn-primary" onClick={handleAddService}>Add Service</button>
      </div>
    </div>
  )}

  {activeSection === 'payment' && (
    <div className="card mb-3">
      <div className="card-header">Add Payment</div>
      <div className="card-body d-flex gap-3 flex-wrap">
        <input className="form-control w-auto" placeholder="Name" value={paymentName} onChange={(e) => setPaymentName(e.target.value)} />
        <input className="form-control w-auto" placeholder="Phone Number" value={paymentPhone} onChange={(e) => setPaymentPhone(e.target.value)} />
        <input className="form-control w-auto" placeholder="Category" value={paymentCategory} onChange={(e) => setPaymentCategory(e.target.value)} />
        <input className="form-control w-auto" placeholder="Service Type" value={paymentServiceType} onChange={(e) => setPaymentServiceType(e.target.value)} />
        <input className="form-control w-auto" type="number" placeholder="Amount" value={paymentAmount} onChange={(e) => setPaymentAmount(e.target.value)} />
        <button className="btn btn-warning" onClick={handleAddPayment}>Add</button>
      </div>
    </div>
  )}
</div>
