const API='/api';
const $=id=>document.getElementById(id);
const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let toastTimer;
function notify(message,type='success'){const t=$('toast');t.textContent=message;t.className='toast '+type;clearTimeout(toastTimer);toastTimer=setTimeout(()=>t.className='toast hidden',4000)}
async function api(path,options={}){const res=await fetch(API+path,{headers:{'Content-Type':'application/json'},...options});if(res.status===204)return null;let data=null;try{data=await res.json()}catch{}if(!res.ok)throw new Error(data?.message||`Request failed (HTTP ${res.status})`);return data}
async function safe(fn){try{await fn()}catch(e){notify(e.message,'error')}}

let currentUser=JSON.parse(localStorage.getItem('campusNgoUser')||'null');
function showAuth(mode='login'){
  $('loginForm').classList.toggle('hidden',mode!=='login');$('registerForm').classList.toggle('hidden',mode!=='register');
  $('authTitle').textContent=mode==='login'?'Welcome back':'Create your account';
  $('authSubtitle').textContent=mode==='login'?'Login to manage your community events.':'Register as an admin or volunteer.';
  $('switchText').textContent=mode==='login'?'New user?':'Already have an account?';$('switchAuth').textContent=mode==='login'?'Create account':'Login';
}
function openDashboard(){
  $('authPage').classList.add('hidden');$('appPage').classList.remove('hidden');
  $('userWelcome').textContent=`${currentUser.name} • ${currentUser.role}`;
  const admin=currentUser.role==='ADMIN';$('adminDashboard').classList.toggle('hidden',!admin);$('volunteerDashboard').classList.toggle('hidden',admin);
  admin?loadAdmin():loadVolunteer();
}
$('switchAuth').addEventListener('click',()=>showAuth($('loginForm').classList.contains('hidden')?'login':'register'));
$('loginForm').addEventListener('submit',e=>{e.preventDefault();safe(async()=>{currentUser=await api('/auth/login',{method:'POST',body:JSON.stringify({email:$('loginEmail').value,password:$('loginPassword').value})});localStorage.setItem('campusNgoUser',JSON.stringify(currentUser));openDashboard()})});
$('registerForm').addEventListener('submit',e=>{e.preventDefault();safe(async()=>{currentUser=await api('/auth/register',{method:'POST',body:JSON.stringify({name:$('regName').value,email:$('regEmail').value,password:$('regPassword').value,phone:$('regPhone').value,role:$('regRole').value})});localStorage.setItem('campusNgoUser',JSON.stringify(currentUser));notify('Account created successfully.');openDashboard()})});
$('logoutBtn').addEventListener('click',()=>{localStorage.removeItem('campusNgoUser');currentUser=null;$('appPage').classList.add('hidden');$('authPage').classList.remove('hidden');$('loginForm').reset();showAuth('login')});

async function loadAdmin(){await Promise.all([loadEvents(),loadVolunteers(),loadSignups(),loadAttendancePage()]);}
async function loadEvents(){const [events,upcoming]=await Promise.all([api('/events'),api('/events/upcoming')]);$('statEvents').textContent=events.length;$('statSlots').textContent=upcoming.reduce((n,e)=>n+e.openSlots,0);$('upcomingRows').innerHTML=upcoming.length?upcoming.map(eventRow).join(''):emptyRow(7,'No upcoming events with open slots.');}
function eventRow(e){return `<tr><td>${esc(e.name)}</td><td>${e.date}</td><td>${esc(e.location)}</td><td>${e.capacity}</td><td>${e.registeredCount}</td><td>${e.openSlots}</td><td><button class="small" onclick="editEvent(${e.id})">Edit</button> <button class="small danger" onclick="deleteEvent(${e.id})">Delete</button></td></tr>`}
function emptyRow(n,text){return `<tr><td colspan="${n}" class="empty">${text}</td></tr>`}
function resetEventForm(){$('eventForm').reset();$('eventId').value='';$('eventFormTitle').textContent='Create event';$('eventSubmit').textContent='Create event'}
async function editEvent(id){await safe(async()=>{const e=await api('/events/'+id);$('eventId').value=e.id;$('eventName').value=e.name;$('eventDate').value=e.date;$('eventLocation').value=e.location;$('eventCapacity').value=e.capacity;$('eventFormTitle').textContent='Edit event #'+id;$('eventSubmit').textContent='Update event';window.scrollTo({top:0,behavior:'smooth'})})}
async function deleteEvent(id){if(!confirm('Delete this event and its signups?'))return;await safe(async()=>{await api('/events/'+id,{method:'DELETE'});notify('Event deleted.');resetEventForm();await loadAdmin()})}
$('eventForm').addEventListener('submit',e=>{e.preventDefault();safe(async()=>{const id=$('eventId').value;const body={name:$('eventName').value,date:$('eventDate').value,location:$('eventLocation').value,capacity:Number($('eventCapacity').value)};await api(id?'/events/'+id:'/events',{method:id?'PUT':'POST',body:JSON.stringify(body)});notify(id?'Event updated.':'Event created.');resetEventForm();await loadAdmin()})});
$('eventReset').addEventListener('click',resetEventForm);
async function loadVolunteers(){const v=await api('/volunteers');$('statVolunteers').textContent=v.length;$('volunteerRows').innerHTML=v.length?v.map(x=>`<tr><td>${esc(x.name)}</td><td>${esc(x.email)}</td><td>${esc(x.phone)}</td><td>${Number(x.totalHours||0).toFixed(2)}</td></tr>`).join(''):emptyRow(4,'No volunteers yet.')}
async function loadSignups(){const s=await api('/signups');$('statSignups').textContent=s.length;$('signupRows').innerHTML=s.length?s.map(x=>`<tr><td>${esc(x.eventName)}</td><td>${esc(x.volunteerName)}</td><td>${esc(x.volunteerEmail)}</td><td>${esc(x.signupDate)}</td><td><button class="small danger" onclick="deleteSignup(${x.id})">Cancel</button></td></tr>`).join(''):emptyRow(5,'No signups yet.')}
async function deleteSignup(id){if(!confirm('Cancel this signup?'))return;await safe(async()=>{await api('/signups/'+id,{method:'DELETE'});notify('Signup cancelled.');await loadAdmin()})}
async function loadAttendancePage(){const events=await api('/events');$('attEvent').innerHTML='<option value="">-- Select event --</option>'+events.map(e=>`<option value="${e.id}">${esc(e.name)} (${e.date})</option>`).join('');await loadAttendanceRows()}
$('attEvent').addEventListener('change',()=>safe(loadAttendanceRows));
async function loadAttendanceRows(){const eventId=$('attEvent').value;if(!eventId){$('attRows').innerHTML=emptyRow(6,'Select an event.');return}const signups=await api('/signups?eventId='+eventId);$('attRows').innerHTML=signups.length?signups.map(s=>{const badge=s.attended===true?'<span class="badge yes">Attended</span>':s.attended===false?'<span class="badge no">Not attended</span>':'<span class="badge none">Not marked</span>';return `<tr><td>${esc(s.volunteerName)}</td><td>${esc(s.volunteerEmail)}</td><td>${badge}</td><td><select id="att-${s.id}"><option value="">-- select --</option><option value="true" ${s.attended===true?'selected':''}>Attended</option><option value="false" ${s.attended===false?'selected':''}>Not attended</option></select></td><td><input type="number" min="0" step="0.25" id="hrs-${s.id}" value="${s.hoursContributed??''}"></td><td><button class="small" onclick="saveAttendance(${s.id},${s.attendanceId??'null'})">Save</button></td></tr>`}).join(''):emptyRow(6,'No volunteers registered for this event.')}
async function saveAttendance(signupId,attendanceId){await safe(async()=>{const a=$('att-'+signupId).value;const h=$('hrs-'+signupId).value;if(a===''){notify('Choose attendance status.','error');return}await api(attendanceId?'/attendance/'+attendanceId:'/attendance',{method:attendanceId?'PUT':'POST',body:JSON.stringify({signupId,attended:a==='true',hoursContributed:h===''?null:Number(h)})});notify('Attendance saved.');await loadAttendanceRows();await loadVolunteers()})}
document.querySelectorAll('[data-admin-section]').forEach(b=>b.addEventListener('click',()=>{document.querySelectorAll('[data-admin-section]').forEach(x=>x.classList.toggle('active',x===b));document.querySelectorAll('.admin-section').forEach(x=>x.classList.toggle('hidden',x.id!=='admin-'+b.dataset.adminSection))}));

async function loadVolunteer(){const id=currentUser.volunteerId;if(!id){notify('Volunteer account is missing its volunteer profile.','error');return}const [events,hours,signups]=await Promise.all([api('/events/upcoming'),api('/volunteers/'+id+'/hours'),api('/signups?volunteerId='+id)]);$('myHours').textContent=Number(hours.totalHours||0).toFixed(2);$('mySignupCount').textContent=signups.length;$('myOpenEvents').textContent=events.length;$('volEventRows').innerHTML=events.length?events.map(e=>`<tr><td>${esc(e.name)}</td><td>${e.date}</td><td>${esc(e.location)}</td><td>${e.openSlots}</td><td><button class="small" onclick="joinEvent(${e.id})" ${e.openSlots===0?'disabled':''}>Sign up</button></td></tr>`).join(''):emptyRow(5,'No upcoming events with open slots.');$('mySignupRows').innerHTML=signups.length?signups.map(s=>`<tr><td>${esc(s.eventName)}</td><td>${s.eventDate||'-'}</td><td>${s.location||'-'}</td><td>${s.attended===true?'<span class="badge yes">Attended</span>':s.attended===false?'<span class="badge no">Not attended</span>':'<span class="badge none">Not marked</span>'}</td><td>${s.hoursContributed==null?'0.00':Number(s.hoursContributed).toFixed(2)}</td></tr>`).join(''):emptyRow(5,'You have no registrations yet.')}
async function joinEvent(eventId){await safe(async()=>{await api('/signups',{method:'POST',body:JSON.stringify({eventId,volunteerId:currentUser.volunteerId})});notify('You are registered for the event.');await loadVolunteer()})}

if(currentUser)openDashboard();else showAuth('login');
