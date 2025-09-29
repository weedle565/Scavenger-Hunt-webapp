const API_BASE = '/api';  // same origin

const teamFormDiv = document.getElementById('teamFormDiv');
const teamForm = document.getElementById('teamForm');
const tasksDiv = document.getElementById('tasksDiv');
const taskListDiv = document.getElementById('taskList');

let score = 0;

function updateScore() {
    document.getElementById("score-value").textContent = score;
}

function setTheme(themeName) {
    if (themeName.toLowerCase() === 'base') {
        document.body.className = ''; // remove all theme classes
    } else {
        document.body.className = `theme-${themeName.toLowerCase()}`;
    }
    localStorage.setItem("theme", themeName);
}

document.addEventListener("DOMContentLoaded", () => {
    const themeSelect = document.getElementById('themeSelect'); // now it's defined

    // Restore saved theme
    const saved = localStorage.getItem("theme") || 'base';
    setTheme(saved);
    themeSelect.value = saved;

    // Add change listener
    themeSelect.addEventListener('change', function () {
        setTheme(this.value); // no need to lowerCase here; already handled in setTheme
    });

    // Init score
    updateScore();
});
function setCookie(name, value) {
    document.cookie = `${name}=${value}; path=/`;
}

// Get local cookies
function getCookie() {
    const match = document.cookie.match(new RegExp(/team=([^;]+)/));

    if (match) return match[1];
    return null;
}

// Request to get cookie
async function getCookieRequest(id) {
    const resp = await fetch(`${API_BASE}/team/${id}/cookie`);
    if (!resp.ok) throw new Error('Failed to generate cookie');
    return resp.text()

}
async function createTeam(name, password) {
    const resp = await fetch(`${API_BASE}/team`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, password}),
    });

    if (!resp.ok) throw new Error('Failed to create team');
    return resp.json();
}

async function fetchTasks() {
    const resp = await fetch(`${API_BASE}/task/all`, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getCookie("team")}`
        }
    });
    if (!resp.ok) throw new Error('Failed to fetch tasks');
    return resp.json();
}


function showTasks(tasks) {
    taskListDiv.innerHTML = '';

    tasks.forEach(task => {
        const taskDiv = document.createElement('div');
        taskDiv.className = 'task';

        const title = document.createElement('h3');
        title.textContent = task.name;

        const content = document.createElement('p');
        content.textContent = '';
        content.style.display = 'none';

        const locationSpan = document.createElement('span');
        locationSpan.textContent = task.location === "Hidden" ? "Hidden" : task.location;

        taskDiv.appendChild(title);
        taskDiv.appendChild(content);
        taskDiv.appendChild(document.createTextNode('Location: '));
        taskDiv.appendChild(locationSpan);
        taskDiv.appendChild(document.createElement('br'));

        // Buttons
        const easyBtn = document.createElement('button');
        easyBtn.textContent = 'Easy Challenge';
        easyBtn.className = 'outline-btn easy-btn';

        const hardBtn = document.createElement('button');
        hardBtn.textContent = 'Hard Challenge';
        hardBtn.className = 'outline-btn hard-btn';

        const completeBtn = document.createElement('button');
        completeBtn.textContent = 'Complete';
        completeBtn.className = 'outline-btn complete-btn';
        completeBtn.style.display = 'none'; // hidden initially

        // Button wrapper
        const buttonWrapper = document.createElement('div');
        buttonWrapper.className = 'button-wrapper';
        buttonWrapper.appendChild(easyBtn);
        buttonWrapper.appendChild(hardBtn);
        buttonWrapper.appendChild(completeBtn);
        taskDiv.appendChild(buttonWrapper);

        // Track difficulty selected
        let selectedDifficulty = null;

        function startChallenge(difficulty) {
            content.style.display = 'block';
            content.textContent = task.description;
            easyBtn.remove();
            hardBtn.remove();
            completeBtn.style.display = 'inline-block';
            selectedDifficulty = difficulty;
        }

        easyBtn.addEventListener('click', () => startChallenge('e'));
        hardBtn.addEventListener('click', () => startChallenge('h'));

        completeBtn.addEventListener('click', async () => {
            if (!selectedDifficulty) return;

            try {
                // Map difficulty to backend path
                const taskType = selectedDifficulty === 'hard' ? 'h' : 'e';
                const url = `${API_BASE}/team/complete/${task.id}/${taskType}`;
                const token = getCookie("team");

                console.log("Completing task:");
                console.log("URL:", url);
                console.log("Token:", token);

                const resp = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    }
                });

                console.log("Fetch response status:", resp.status);

                if (!resp.ok) {
                    const text = await resp.text(); // log the server response
                    console.error("Server response:", text);
                    throw new Error(`Failed to complete task: ${resp.status}`);
                }

                // this is the total points after completion
                score = await resp.json();
                updateScore();

                // Animate task removal
                taskDiv.classList.add('slide-out-right');
                taskDiv.addEventListener('animationend', () => taskDiv.remove(), { once: true });

            } catch (err) {
                alert('Failed to complete task: ' + err.message);
            }
        });

        taskListDiv.appendChild(taskDiv);
    });
}

async function submitCompletion(taskId, taskCard, taskDifficulty) {
    const teamId = getCookie('team');
    if (!teamId) return;

    taskCard.classList.add('slide-out-right');
    taskCard.addEventListener('animationend', () => {
        taskCard.remove();
    }, { once: true });

}

// Admin stuff
function isAdminLoggedIn() {
    return !!localStorage.getItem('adminToken');
}

// Show admin toggle only if logged in
const adminToggle = document.getElementById('adminToggle');
adminToggle.addEventListener('click', () => {
    if (!isAdminLoggedIn()) {
        alert('Please log in as admin first!');
        window.location.href = '/admin-login.html';
        return;
    }

    const panel = document.getElementById('adminTaskDiv');
    panel.style.display = panel.style.display === 'none' ? 'block' : 'none';

    if (panel.style.display === 'block') {
        loadAdminPanel();
    }
});

// API calls
async function fetchAllTasks() {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    const resp = await fetch(`${API_BASE}/task/all`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    return await resp.json();
}

async function fetchAllTeams() {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    const resp = await fetch(`${API_BASE}/team/all`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return await resp.json();
}

async function deleteTask(id) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    if (!confirm("Are you sure you want to delete this task?")) return;
    await fetch(`${API_BASE}/task/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    await loadAdminPanel();
}

async function deleteTeam() {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    if (!confirm("Are you sure you want to delete this team?")) return;
    await fetch(`${API_BASE}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    await loadAdminPanel();
}

async function createTaskFromAdmin(data) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    await fetch(`${API_BASE}/task`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    });
    await loadAdminPanel();
}

// Load admin panel content
// async function loadAdminPanel() {
//     const tasks = await fetchAllTasks();
//     const teams = await fetchAllTeams();
//
//     const adminTasksList = document.getElementById('adminTaskList');
//     adminTasksList.innerHTML = '';
//
//     const adminTasksDiv = document.getElementById('adminTaskDiv');
//     // TASKS
//     const taskTitle = document.createElement('h2');
//     taskTitle.textContent = 'Tasks';
//     adminTasksDiv.appendChild(taskTitle);
//
//     tasks.forEach(task => {
//         const div = document.createElement('div');
//         div.className = 'task';
//
//         // Create and append title
//         const title = document.createElement('h3');
//         title.textContent = task.name;
//         div.appendChild(title);
//
//         // Create and append riddle paragraph
//         const riddle = document.createElement('p');
//         riddle.textContent = task.riddle;
//         div.appendChild(riddle);
//
//         // Create and append location span
//         const location = document.createElement('span');
//         location.textContent = task.location;
//         div.appendChild(location);
//
//         div.appendChild(document.createElement('br'));
//
//         // Create and append delete button
//         const btn = document.createElement('button');
//         btn.textContent = 'Delete';
//         btn.addEventListener('click', () => deleteTask(task.id));
//         div.appendChild(btn);
//
//         adminTasksDiv.appendChild(div);
//     });
//
//     // TEAMS
//     const teamTitle = document.createElement('h2');
//     teamTitle.textContent = 'Teams';
//     adminTasksDiv.appendChild(teamTitle);
//
//     teams.forEach(team => {
//         const div = document.createElement('div');
//         div.className = 'task';
//
//         const strong = document.createElement('strong');
//         strong.textContent = team.name;
//
//         const idText = document.createElement('div');
//         idText.textContent = `(ID: ${team.id})`;
//
//         const btn = document.createElement('button');
//         btn.textContent = 'Delete';
//         btn.addEventListener('click', () => deleteTeam(team.id));
//
//         div.appendChild(strong);
//         div.appendChild(document.createElement('br'));
//         div.appendChild(idText);
//         div.appendChild(document.createElement('br'));
//         div.appendChild(btn);
//
//         adminTasksDiv.appendChild(div);
//     });
//
// }

document.getElementById('hasSubtask').addEventListener('change', (e) => {
    document.getElementById('subtaskFields').style.display = e.target.checked ? 'block' : 'none';
});

document.getElementById('adminTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;

    const data = {
        name: form.name.value,
        riddle: form.riddle.value,
        description: form.description.value,
        location: form.location.value,
        completed: false,
        hasSubtask: false,
        subtaskComplete: false,
    };

    if (form.hasSubtask.checked) {
        data.hasSubtask = true;
        data.subtaskDescription = form.subtaskDescription.value

    }

    await createTaskFromAdmin(data);
    form.reset();
    document.getElementById('subtaskFields').style.display = 'none';  // Hide again after submit
});

const themeSwitcher = document.getElementById('themeSwitcher');
const toggleBtn = document.getElementById('themeToggle');

toggleBtn.addEventListener('click', () => {
    themeSwitcher.classList.toggle('open');
});

// On page load check for teamId cookie or show form
async function init() {
    let teamId = getCookie('team');

    if (!teamId) {
        teamFormDiv.style.display = 'block';
        tasksDiv.style.display = 'none';

        teamForm.onsubmit = async e => {
            e.preventDefault();
            const name = document.getElementById('teamName').value.trim();
            if (!name) return alert('Please enter a team name');
            const pass = document.getElementById('password').value.trim();
            if (!pass) return alert("Please enter a password");

            try {
                const team = await createTeam(name, pass);

                const cookie = (await getCookieRequest(team.name))

                setCookie('team', cookie);
                teamId = team.id;
                teamFormDiv.style.display = 'none';
                tasksDiv.style.display = 'block';

                const tasks = await fetchTasks(cookie);

                showTasks(tasks, teamId);
            } catch (err) {
                alert('Failed to create team: ' + err.message);
            }
        };

    } else {
        // Already have teamId cookie, show tasks directly
        try {
            const tasks = await fetchTasks(teamId);
            teamFormDiv.style.display = 'none';
            tasksDiv.style.display = 'block';

            showTasks(tasks, teamId);
        } catch {
            // Invalid teamId in cookie: reset and show form
            setCookie('team', '', -1);
            teamFormDiv.style.display = 'block';
            tasksDiv.style.display = 'none';
        }
    }

}



window.onload = init;