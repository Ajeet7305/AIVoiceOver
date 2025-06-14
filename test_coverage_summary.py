#!/usr/bin/env python3
"""
Test Coverage Summary Report
Generates a test summary report similar to IDE test runners
"""
import xml.etree.ElementTree as ET
import glob
import os
import sys

def find_test_results():
    """Find all test result XML files"""
    return glob.glob('aivoice/build/test-results/**/TEST-*.xml', recursive=True)

def find_jacoco_report():
    """Find JaCoCo coverage XML file"""
    xml_files = glob.glob('aivoice/build/**/jacoco*.xml', recursive=True)
    return xml_files[0] if xml_files else None

def parse_test_results():
    """Parse test result XML files to get package-level statistics"""
    test_files = find_test_results()
    if not test_files:
        print("❌ No test result files found. Please run tests first: ./gradlew :aivoice:test")
        return {}, 0, 0, 0, 0.0
    
    package_stats = {}
    total_tests = 0
    total_failures = 0
    total_ignored = 0
    total_duration = 0.0
    
    for test_file in test_files:
        try:
            tree = ET.parse(test_file)
            root = tree.getroot()
            
            # Extract package name from classname
            for testcase in root.findall('.//testcase'):
                classname = testcase.get('classname', '')
                if not classname:
                    continue
                
                # Extract package name (everything except the last part)
                parts = classname.split('.')
                if len(parts) > 1:
                    package = '.'.join(parts[:-1])
                else:
                    package = classname
                
                if package not in package_stats:
                    package_stats[package] = {
                        'tests': 0,
                        'failures': 0,
                        'ignored': 0,
                        'duration': 0.0
                    }
                
                package_stats[package]['tests'] += 1
                total_tests += 1
                
                # Check for failures/errors
                if testcase.find('failure') is not None or testcase.find('error') is not None:
                    package_stats[package]['failures'] += 1
                    total_failures += 1
                
                # Check for skipped/ignored
                if testcase.find('skipped') is not None:
                    package_stats[package]['ignored'] += 1
                    total_ignored += 1
                
                # Add duration
                duration = float(testcase.get('time', '0'))
                package_stats[package]['duration'] += duration
                total_duration += duration
                
        except Exception as e:
            print(f"Warning: Error parsing {test_file}: {e}")
            continue
    
    return package_stats, total_tests, total_failures, total_ignored, total_duration

def get_coverage_info():
    """Get overall coverage information"""
    jacoco_file = find_jacoco_report()
    if not jacoco_file:
        return None
    
    try:
        tree = ET.parse(jacoco_file)
        root = tree.getroot()
        
        for counter in root.findall('./counter[@type="INSTRUCTION"]'):
            missed = int(counter.get('missed', 0))
            covered = int(counter.get('covered', 0))
            total = missed + covered
            percentage = (covered / total * 100) if total > 0 else 0
            return {
                'percentage': percentage,
                'covered': covered,
                'total': total,
                'missed': missed
            }
    except Exception as e:
        print(f"Warning: Error reading coverage data: {e}")
    
    return None

def generate_test_summary():
    """Generate the test summary report"""
    package_stats, total_tests, total_failures, total_ignored, total_duration = parse_test_results()
    
    if total_tests == 0:
        return
    
    print()
    print("Test Summary")
    print()
    
    # Calculate success rate
    success_rate = ((total_tests - total_failures) / total_tests * 100) if total_tests > 0 else 0
    
    # Summary boxes side by side
    print("┌─────────────────────────────────────────────┐    ┌─────────────┐")
    print(f"│                                             │    │             │")
    print(f"│    {total_tests:<8} {total_failures:<8} {total_ignored:<8} {total_duration:.3f}s    │    │    {success_rate:.0f}%     │")
    print(f"│    tests     failures  ignored   duration  │    │ successful  │")
    print(f"│                                             │    │             │")
    print("└─────────────────────────────────────────────┘    └─────────────┘")
    print()
    
    # Tab headers
    print("┌─────────────┐ ┌─────────────┐")
    print("│  Packages   │ │   Classes   │")
    print("└─────────────┘ └─────────────┘")
    print()
    
    # Table header
    print(f"{'Package':<50} {'Tests':<6} {'Failures':<9} {'Ignored':<9} {'Duration':<10} {'Success rate'}")
    print("─" * 100)
    
    # Sort packages by name
    sorted_packages = sorted(package_stats.items())
    
    for package, stats in sorted_packages:
        tests = stats['tests']
        failures = stats['failures']
        ignored = stats['ignored']
        duration = stats['duration']
        
        # Calculate success rate for this package
        pkg_success_rate = ((tests - failures) / tests * 100) if tests > 0 else 0
        
        # Shorten package name for better display
        pkg_display = package.replace('com.aivoiceclassifier.', '')
        if len(pkg_display) > 48:
            pkg_display = pkg_display[:45] + "..."
        
        print(f"{pkg_display:<50} {tests:<6} {failures:<9} {ignored:<9} {duration:.3f}s{'':<5} {pkg_success_rate:.0f}%")
    
    print()
    
    # Coverage information
    coverage_info = get_coverage_info()
    if coverage_info:
        print("Coverage Information:")
        print(f"Overall Instruction Coverage: {coverage_info['percentage']:.1f}% ({coverage_info['covered']:,} of {coverage_info['total']:,} instructions)")
        
        # Coverage status
        if coverage_info['percentage'] >= 80:
            status = "🟢 Excellent"
        elif coverage_info['percentage'] >= 50:
            status = "🟡 Good"
        elif coverage_info['percentage'] >= 20:
            status = "🟠 Needs Improvement"
        else:
            status = "🔴 Critical"
        
        print(f"Coverage Status: {status}")
    else:
        print("Coverage Information: Not available (run: ./gradlew :aivoice:jacocoTestReport)")
    
    print()

if __name__ == "__main__":
    generate_test_summary()