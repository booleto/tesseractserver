const reader = new FileReader();

form = document.getElementById("myForm")

function readFile(file) {
    return function(resolve) {
        reader.readAsBinaryString(file);
        reader.onload = function() {
            var arrayBuffer = reader.result
            // console.log(arrayBuffer)
            // var bytes = new Uint8Array(arrayBuffer);
            // resolve(bytes);
            resolve(arrayBuffer)
        }
    }
}

form.onsubmit = async (e) => {
    e.preventDefault();

    document.getElementById('ketquahead').innerHTML = "loading...";
    
    const file = document.getElementById("myFile").files[0];

    let formData = new FormData();
    formData.append("body", "oof")

    formData.append("uploaded_file", file)

    let response = await fetch('http://10.15.10.122:12321/read/pdf/tika', {
        method: 'POST',
        body: formData
    })
    // .then((response) => {
    //     let result = response.text;
    //     console.log(result)
    //     document.getElementById('contents').innerHTML = result;
    // })

    let result = await response.json();
    console.log(result)
    document.getElementById('ketquahead').innerHTML = "Đọc thành công!";
    // document.getElementById('ketquahead').innerHTML = JSON.stringify(result, null, 4);
    // let result = await response.json;

    // var style = `table {
    //     font-family: arial, sans-serif;
    //     border-collapse: collapse;
    //     width: 100%;
    //   }
      
    //   td, th {
    //     border: 1px solid #dddddd;
    //     text-align: left;
    //     padding: 8px;
    //   }`;

    var resultTable = "<table style=\"width:100%\">"
    resultTable += "<tr> <th>Trang</th> <th>Dòng</th> <th>Nội dung</th> </tr>";
    for (var page in result) {
        var lines = result[page]
        print("line: " + lines)
        for (var line in result[page]) {
            console.log(line)
            if (result[page].hasOwnProperty(line)) {
                resultTable += "<tr> <th>" + page + "</th> <th>" + line + "</th> <th>" + result[page][line] + "</th> </tr>";
            }
        }
    }
    resultTable += "</table>"
    document.write(resultTable)
}

// form.onsubmit = async (e) => {
//     e.preventDefault();

//     document.getElementById('contents').innerHTML = "loading...";
    
//     const file = document.getElementById("myFile").files[0];
//     let upfile = new Promise(readFile(file));

//     let formData = new FormData();
//     formData.append("body", "oof")

//     upfile.then(async function(fileData) {
//         formData.append("uploaded_file", fileData)

//         let response = await fetch('http://localhost:12345/read/pdf/tika', {
//             method: 'POST',
//             body: formData
//         })
//         let result = response.json;
//         document.getElementById('contents').innerHTML = result;

//     }).catch(function(err) {
//         console.log(err)
//     })
// }

// form.onsubmit = async (e) => {
//     e.preventDefault();

//     var formdata = new FormData();
//     formdata.append("body", "lmao");
//     formdata.append("uploaded_file", fileInput.files[0]);
    
//     var requestOptions = {
//       method: 'POST',
//       body: formdata,
//       redirect: 'follow'
//     };
    
//     fetch("http://0.0.0.0:12345/read/pdf/tika", requestOptions)
//       .then(response => response.text())
//       .then(result => console.log(result))
//       .catch(error => console.log('error', error));
// }



function sendFile() {
    var xhr = new XMLHttpRequest();
    var form = document.getElementById("Form");

    xhr.addEventListener("load", (event) => {
        alert("Success");
    });

    xhr.addEventListener("error", (event) => {
        alert("Error")
    });

    xhr.open("POST", "http://0.0.0.0:12345/read/pdf/tika");

    xhr.setRequestHeader(
        "Content-Type",
        `multipart/formdata; boundary=\n`
    )

    xhr.send()

    form.append("body", "oof");
    form.append("uploaded_file", reader.result)

}
