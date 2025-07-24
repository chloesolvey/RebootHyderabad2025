document.addEventListener('DOMContentLoaded', function () {
    const emissionFactors = {
        home: {
            electricity: 0.177, // kg CO2e per kWh
            heating: {
                gas: 0.2027, // kg CO2e per kWh (Net CV)
                lpg: 0.23032, // kg CO2e per kWh (Net CV)
                none: 0.0
            }
        },
        transport: {
            car: {
                none: 0.0,
                petrol_supermini: 0.14276,
                diesel_supermini: 0.13452,
                phev_supermini: 0.03008 + 0.02408 + 0.00252, // Scope 1 + Scope 2 + Scope 3 T&D
                bev_supermini: 0 + 0.03419 + 0.00357, // Scope 1 (0) + Scope 2 + Scope 3 T&D
                petrol_luxury: 0.30752,
                diesel_luxury: 0.20632
            },
            bus: 0.12525, // local bus not london, per passenger.km
            rail: 0.03546, // national rail, per passenger.km
            air: {
                short: 0.12576 * 1500, // per passenger.km, assuming 1500km avg return flight
                long: 0.11704 * 10000 // per passenger.km, assuming 10000km avg return flight
            }
        },
        consumption: {
            waste_landfill: 0.70030886, // organic waste, kg co2e per kg (converted from tonne)
            waste_recycled: 0.00898311, // composted, kg co2e per kg
        }
    };

    const form = document.getElementById('footprintForm');
    const allInputs = form.querySelectorAll('input, select');
    
    let footprintPieChart, transportBarChart;

    function calculateFootprint() {
        const electricity = parseFloat(document.getElementById('electricity').value) || 0;
        const heatingType = document.getElementById('heating').value;
        const heatingKwh = parseFloat(document.getElementById('heatingKwh').value) || 0;
        const carType = document.getElementById('carType').value;
        const mileage = parseFloat(document.getElementById('mileage').value) || 0;
        const bus = parseFloat(document.getElementById('bus').value) || 0;
        const rail = parseFloat(document.getElementById('rail').value) || 0;
        const waste = parseFloat(document.getElementById('waste').value) || 0;
        const recycling = parseFloat(document.getElementById('recycling').value) || 0;
        const shortHaulFlights = parseFloat(document.getElementById('shortHaul').value) || 0;
        const longHaulFlights = parseFloat(document.getElementById('longHaul').value) || 0;
        
        const homeEmissions = (electricity * emissionFactors.home.electricity) + (heatingKwh * emissionFactors.home.heating[heatingType]);
        const carEmissions = mileage * emissionFactors.transport.car[carType];
        const publicTransportEmissions = (bus * emissionFactors.transport.bus) + (rail * emissionFactors.transport.rail);
        const flightEmissions = ((shortHaulFlights * emissionFactors.transport.air.short) + (longHaulFlights * emissionFactors.transport.air.long)) / 52.14; // annual to weekly
        const transportEmissions = carEmissions + publicTransportEmissions + flightEmissions;
        const consumptionEmissions = (waste * emissionFactors.consumption.waste_landfill) + (recycling * emissionFactors.consumption.waste_recycled);

        const totalFootprint = homeEmissions + transportEmissions + consumptionEmissions;

        return {
            home: homeEmissions,
            transport: transportEmissions,
            consumption: consumptionEmissions,
            total: totalFootprint,
            carMileage: mileage
        };
    }

    function updateDashboard(data) {
        document.getElementById('totalFootprint').textContent = data.total.toFixed(2);
        updatePieChart(data);
        updateTransportChart(data.carMileage);
    }
    
    function updatePieChart(data) {
        const chartData = {
            labels: ['Home Energy', 'Transport', 'Consumption'],
            datasets: [{
                data: [data.home.toFixed(2), data.transport.toFixed(2), data.consumption.toFixed(2)],
                backgroundColor: ['#00B4D8', '#0077B6', '#90E0EF'],
                borderColor: '#FFFFFF',
                borderWidth: 2,
                hoverOffset: 4
            }]
        };

        if (footprintPieChart) {
            footprintPieChart.data = chartData;
            footprintPieChart.update();
        } else {
            const ctx = document.getElementById('footprintPieChart').getContext('2d');
            footprintPieChart = new Chart(ctx, {
                type: 'doughnut',
                data: chartData,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    let label = context.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    if (context.parsed !== null) {
                                        label += context.parsed.toFixed(2) + ' kg CO₂e';
                                    }
                                    return label;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    function updateTransportChart(mileage) {
        const carType = document.getElementById('carType').value;
        
        const chartData = {
            labels: ['Your Car', 'BEV (Supermini)', 'National Rail', 'Local Bus'],
            datasets: [{
                label: 'Weekly kg CO₂e for ' + mileage + ' km',
                data: [
                    mileage * emissionFactors.transport.car[carType],
                    mileage * emissionFactors.transport.car['bev_supermini'],
                    mileage * emissionFactors.transport.rail,
                    mileage * emissionFactors.transport.bus
                ],
                backgroundColor: ['#0077B6', '#00B4D8', '#90E0EF', '#CAF0F8'],
                borderColor: ['#023E8A', '#023E8A', '#023E8A', '#023E8A'],
                borderWidth: 1,
                borderRadius: 5
            }]
        };

        if(transportBarChart) {
            transportBarChart.data = chartData;
            transportBarChart.options.plugins.title.text = 'Weekly kg CO₂e for ' + mileage + ' km';
            transportBarChart.update();
        } else {
             const ctx = document.getElementById('transportBarChart').getContext('2d');
             transportBarChart = new Chart(ctx, {
                type: 'bar',
                data: chartData,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    indexAxis: 'y',
                    scales: {
                        x: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'kg CO₂e'
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        },
                        title: {
                            display: false,
                        }
                    }
                }
            });
        }
    }

    // Variable to store the logged-in username
    let currentUsername = '';

    // Main Page Elements
    const infographicPage = document.getElementById('infographicPage');

    // Login Logic
    const loginForm = document.getElementById('loginForm');
    const loginPage = document.getElementById('loginPage');
    const mainApp = document.getElementById('mainApp');
    const usernameInput = document.getElementById('username'); // Get reference to username input
    const passwordInput = document.getElementById('password'); // Get reference to password input
    const loginError = document.getElementById('loginError');

    // Infographic Page Login Link
    const infographicLoginLink = document.getElementById('infographicLoginLink');
    infographicLoginLink.addEventListener('click', function(event) {
        event.preventDefault();
        infographicPage.style.display = 'none';
        loginPage.style.display = 'flex'; // Show login page
        mainApp.style.display = 'none'; // Ensure main app is hidden
    });

    loginForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        const enteredUsername = usernameInput.value;
        const enteredPassword = passwordInput.value;

        // Regular expression to match usernames like user_A, user_B, ..., user_T
        const usernamePattern = /^user_[A-T]$/;

        if (enteredUsername === 'admin' && enteredPassword === 'Reboot@2025') {
            currentUsername = 'admin'; // Set currentUsername to admin
            loginPage.style.display = 'none';
            mainApp.style.display = 'block'; // Show main application
            updateDashboard(calculateFootprint()); // Initialize calculator data
        } else if (usernamePattern.test(enteredUsername) && enteredPassword === 'Reboot@2025') {
            currentUsername = enteredUsername;
            loginPage.style.display = 'none';
            mainApp.style.display = 'block'; // Show main application
            updateDashboard(calculateFootprint()); // Initialize calculator data
        } else {
            loginError.classList.remove('hidden');
            loginError.textContent = 'Invalid username or password.';
        }
    });

    // Logout Functionality (now attached to an anchor tag)
    const logoutLink = document.getElementById('logoutLink'); // Get reference to the new anchor tag
    logoutLink.addEventListener('click', function(event) {
        event.preventDefault(); // Prevent default link behavior
        currentUsername = ''; // Clear the stored username
        usernameInput.value = ''; // Clear username field
        passwordInput.value = ''; // Clear password field
        loginError.classList.add('hidden'); // Hide any error messages
        mainApp.style.display = 'none'; // Hide main application
        infographicPage.style.display = 'block'; // Show infographic page
    });


    // Accordion Logic
    const accordionButtons = document.querySelectorAll('.accordion-button');
    accordionButtons.forEach(button => {
        button.addEventListener('click', () => {
            const content = button.nextElementSibling;
            const icon = button.querySelector('.accordion-icon');

            // Close other accordions
            accordionButtons.forEach(otherButton => {
                if (otherButton !== button) {
                    otherButton.nextElementSibling.style.maxHeight = null;
                    otherButton.querySelector('.accordion-icon').textContent = '+';
                    otherButton.querySelector('.accordion-icon').style.transform = 'rotate(0deg)';
                }
            });

            if (content.style.maxHeight) {
                content.style.maxHeight = null;
                icon.textContent = '+';
                icon.style.transform = 'rotate(0deg)';
            } else {
                content.style.maxHeight = content.scrollHeight + "px";
                icon.textContent = '-';
                icon.style.transform = 'rotate(180deg)';
            }
        });
    });

    // Event listeners for calculator inputs (only active when main app is visible)
    allInputs.forEach(input => {
        input.addEventListener('input', () => {
            if (mainApp.style.display === 'block') { // Only calculate if main app is visible
                const data = calculateFootprint();
                updateDashboard(data);
            }
        });
    });

    // Get the view report link and add an event listener for dynamic URL
    // View Full Report Link (for Calculator page)
    const viewReportLink = document.getElementById('viewReportLink');
    viewReportLink.addEventListener('click', function(event) {
        event.preventDefault();
        let targetUrl = '';
        if (currentUsername === 'admin') {
            targetUrl = 'https://lookerstudio.google.com/reporting/70401d40-acad-493b-9e6f-78944d0819f9'; // Admin specific URL
        } else if (currentUsername) {
            const baseUrl = 'https://lookerstudio.google.com/u/1/reporting/4a735a43-c8d6-4269-98fb-d35324b41f47/page/gGRQF';
            targetUrl = `${baseUrl}?params={"ds0.filter_user":"${currentUsername}"}`; // User_A to User_T URL
        } else {
            console.log('Please log in first to view the full report.');
            loginError.textContent = 'Please log in first to view the full report.';
            loginError.classList.remove('hidden');
            return; // Exit if not logged in
        }
        window.open(targetUrl, '_blank');
    });

    // Chart.js specific functions for the Infographic page
    const chartTooltipCallback = {
        plugins: {
            tooltip: {
                callbacks: {
                    title: function(tooltipItems) {
                        const item = tooltipItems[0];
                        let label = item.chart.data.labels[item.dataIndex];
                        if (Array.isArray(label)) {
                            return label.join(' ');
                        } else {
                            return label;
                        }
                    }
                }
            }
        }
    };
    
    function wrapLabels(label) {
        const max_width = 16;
        if (label.length <= max_width) {
            return label;
        }
        let words = label.split(' ');
        let lines = [];
        let currentLine = '';
        words.forEach(word => {
            if ((currentLine + ' ' + word).length > max_width) {
                lines.push(currentLine);
                currentLine = word;
            } else {
                currentLine = currentLine ? currentLine + ' ' + word : word;
            }
        });
        lines.push(currentLine);
        return lines;
    }

    // Initialize Infographic charts (only if infographicPage is visible initially or becomes visible)
    function initializeInfographicCharts() {
        // Transport Chart
        const transportCtx = document.getElementById('transportChart')?.getContext('2d');
        if (transportCtx) {
            new Chart(transportCtx, {
                type: 'bar',
                data: {
                    labels: ['Domestic Flight (Avg, with RF)', 'Regular Taxi', 'Petrol Car (Supermini)', 'Diesel Car (Supermini)', 'Local London Bus', 'National Rail', 'International Rail'].map(wrapLabels),
                    datasets: [{
                        label: 'kg CO₂e per passenger.km',
                        data: [0.22928, 0.14861, 0.14276, 0.13452, 0.06875, 0.03546, 0.00446],
                        backgroundColor: ['#0369A1', '#0EA5E9', '#38BDF8', '#7DD3FC', '#BAE6FD', '#E0F2FE', '#F0F9FF'],
                        borderColor: '#0C4A6E',
                        borderWidth: 1
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'kg CO₂e per passenger.km'
                            }
                        }
                    },
                    plugins: {
                        ...chartTooltipCallback.plugins,
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }

        // Air Travel Chart
        const airTravelCtx = document.getElementById('airTravelChart')?.getContext('2d');
        if (airTravelCtx) {
            new Chart(airTravelCtx, {
                type: 'doughnut',
                data: {
                    labels: ['Long-haul First Class (with RF)', 'Long-haul Economy (with RF)'],
                    datasets: [{
                        label: 'kg CO₂e per passenger.km',
                        data: [0.46814, 0.11704],
                        backgroundColor: ['#075985', '#38BDF8'],
                        borderColor: '#FFFFFF',
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        ...chartTooltipCallback.plugins,
                        legend: {
                            position: 'bottom',
                        }
                    }
                }
            });
        }

        // Waste Chart
        const wasteCtx = document.getElementById('wasteChart')?.getContext('2d');
        if (wasteCtx) {
            new Chart(wasteCtx, {
                type: 'bar',
                data: {
                    labels: ['Landfill (Organic Waste)', 'Composting (Organic Waste)'],
                    datasets: [{
                        label: 'kg CO₂e per tonne',
                        data: [700.30886, 8.98311],
                        backgroundColor: ['#075985', '#38BDF8'],
                        barPercentage: 0.5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'kg CO₂e per tonne of waste'
                            }
                        }
                    },
                    plugins: {
                        ...chartTooltipCallback.plugins,
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }

        // Material Use Chart
        const materialCtx = document.getElementById('materialUseChart')?.getContext('2d');
        if (materialCtx) {
            new Chart(materialCtx, {
                type: 'bar',
                data: {
                    labels: ['Steel Cans', 'Plastics'],
                    datasets: [
                        {
                            label: 'Primary Production',
                            data: [2863.90131, 3172.49932],
                            backgroundColor: '#075985',
                        },
                        {
                            label: 'Closed-Loop Recycled',
                            data: [1823.90131, 1575.39106],
                            backgroundColor: '#38BDF8',
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'kg CO₂e per tonne of material'
                            }
                        }
                    },
                    plugins: {
                        ...chartTooltipCallback.plugins,
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });
        }
    }

    // Initial state: Show infographic page, hide login and main app
    infographicPage.style.display = 'block';
    loginPage.style.display = 'none';
    mainApp.style.display = 'none';

    // Initialize infographic charts on initial load
    initializeInfographicCharts();

    // Add event listeners for navigation within the mainApp (calculator page)
    const calculatorLink = document.getElementById('calculatorLink');
    const dashboardLink = document.getElementById('dashboardLink');
    const hotspotsLink = document.getElementById('hotspotsLink');
    const solutionsLink = document.getElementById('solutionsLink');

    calculatorLink.addEventListener('click', (event) => {
        event.preventDefault();
        document.getElementById('calculator').scrollIntoView({ behavior: 'smooth' });
    });
    dashboardLink.addEventListener('click', (event) => {
        event.preventDefault();
        document.getElementById('dashboard').scrollIntoView({ behavior: 'smooth' });
    });
    hotspotsLink.addEventListener('click', (event) => {
        event.preventDefault();
        document.getElementById('hotspots').scrollIntoView({ behavior: 'smooth' });
    });
    solutionsLink.addEventListener('click', (event) => {
        event.preventDefault();
        document.getElementById('solutions').scrollIntoView({ behavior: 'smooth' });
    });
});